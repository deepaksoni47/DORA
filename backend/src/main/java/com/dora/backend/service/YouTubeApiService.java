package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class YouTubeApiService {

    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final Logger logger = LoggerFactory.getLogger(YouTubeApiService.class);
    private static final Set<String> NEGATIVE_VIDEO_KEYWORDS = Set.of(
            "quilting",
            "sewing",
            "embroidery",
            "washing machine",
            "dishwasher",
            "repair",
            "tractor",
            "lathe",
            "espresso",
            "needle",
            "longarm",
            "excavator",
            "heavy machinery",
            "construction",
            "induction cooker",
            "cooker",
            "bernina",
            "stitch regulator",
            "ruler work kit");
    private static final Set<String> POSITIVE_VIDEO_KEYWORDS = Set.of(
            "lecture",
            "tutorial",
            "course",
            "university",
            "college",
            "computer science",
            "machine learning",
            "data structures",
            "algorithms",
            "operating systems",
            "nptel",
            "mit",
            "stanford",
            "harvard");
    private static final Set<String> ACADEMIC_CHANNEL_KEYWORDS = Set.of(
            "university",
            "college",
            "nptel",
            "stanford",
            "mit",
            "harvard",
            "gate smashers",
            "freecodecamp",
            "geeksforgeeks",
            "codebasics",
            "coursera");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String youtubeApiKey;

    public YouTubeApiService(ObjectMapper objectMapper, @Value("${youtube.api.key:}") String youtubeApiKey) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.youtubeApiKey = youtubeApiKey;
    }

    public List<Document> searchYouTube(String query) {
        if (query == null || query.trim().isEmpty() || youtubeApiKey == null || youtubeApiKey.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(YOUTUBE_SEARCH_URL)
                    .queryParam("part", "snippet")
                    .queryParam("q", query.trim())
                    .queryParam("type", "video")
                    .queryParam("maxResults", 25)
                    .queryParam("key", youtubeApiKey)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");
            if (!items.isArray()) {
                return Collections.emptyList();
            }

            List<Document> documents = new ArrayList<>();
            int currentYear = Year.now().getValue();
            List<String> significantTerms = extractSignificantTerms(query);

            for (JsonNode item : items) {
                String videoId = item.path("id").path("videoId").asText();
                String title = item.path("snippet").path("title").asText();
                String description = item.path("snippet").path("description").asText();
                String channelTitle = item.path("snippet").path("channelTitle").asText();

                if (videoId == null || videoId.isBlank()) {
                    continue;
                }

                if (!isRelevantVideo(title, description, channelTitle, significantTerms, query)) {
                    continue;
                }

                Document document = new Document();
                document.setTitle(title);
                document.setDescription(description);
                document.setUrl("https://www.youtube.com/watch?v=" + videoId);
                document.setSource("youtube");
                document.setType("video");
                document.setYear(currentYear);
                document.setScore(calculateScore(title, description, channelTitle, significantTerms, query));
                documents.add(document);
            }

            return documents;
        } catch (Exception ex) {
            logger.error("Error while fetching YouTube results", ex);
            return Collections.emptyList();
        }
    }

    private boolean isRelevantVideo(
            String title,
            String description,
            String channelTitle,
            List<String> significantTerms,
            String query) {
        String normalizedTitle = normalize(title);
        String normalizedDescription = normalize(description);
        String normalizedChannel = normalize(channelTitle);
        String normalizedQuery = normalize(query);

        int matchedTerms = countMatches(normalizedTitle, normalizedDescription, significantTerms);
        boolean phraseMatch = !normalizedQuery.isBlank()
                && (normalizedTitle.contains(normalizedQuery) || normalizedDescription.contains(normalizedQuery));
        boolean negativeSignal = containsAny(normalizedTitle, normalizedDescription, normalizedChannel, NEGATIVE_VIDEO_KEYWORDS);
        boolean positiveSignal = containsAny(normalizedTitle, normalizedDescription, normalizedChannel, POSITIVE_VIDEO_KEYWORDS);
        boolean academicChannelSignal = containsAny(normalizedChannel, normalizedChannel, normalizedChannel, ACADEMIC_CHANNEL_KEYWORDS);

        if (negativeSignal && !positiveSignal) {
            return false;
        }

        if (significantTerms.isEmpty()) {
            return positiveSignal;
        }

        if (phraseMatch) {
            return true;
        }

        if (significantTerms.size() == 1) {
            return matchedTerms >= 1 && (positiveSignal || academicChannelSignal);
        }

        if (matchedTerms >= Math.min(2, significantTerms.size())) {
            return true;
        }

        return matchedTerms >= 1 && positiveSignal && academicChannelSignal;
    }

    private double calculateScore(
            String title,
            String description,
            String channelTitle,
            List<String> significantTerms,
            String query) {
        String normalizedTitle = normalize(title);
        String normalizedDescription = normalize(description);
        String normalizedChannel = normalize(channelTitle);
        String normalizedQuery = normalize(query);

        int matchedTerms = countMatches(normalizedTitle, normalizedDescription, significantTerms);
        double score = 25.0 + (matchedTerms * 12.0);

        if (!normalizedQuery.isBlank()
                && (normalizedTitle.contains(normalizedQuery) || normalizedDescription.contains(normalizedQuery))) {
            score += 20.0;
        }

        if (containsAny(normalizedTitle, normalizedDescription, normalizedChannel, POSITIVE_VIDEO_KEYWORDS)) {
            score += 12.0;
        }

        if (normalizedChannel.contains("university")
                || normalizedChannel.contains("nptel")
                || normalizedChannel.contains("stanford")
                || normalizedChannel.contains("mit")
                || normalizedChannel.contains("harvard")
                || normalizedChannel.contains("freecodecamp")
                || normalizedChannel.contains("gate smashers")
                || normalizedChannel.contains("geeksforgeeks")
                || normalizedChannel.contains("codebasics")) {
            score += 8.0;
        }

        return score;
    }

    private List<String> extractSignificantTerms(String query) {
        return java.util.Arrays.stream(normalize(query).split(" "))
                .filter(token -> !token.isBlank())
                .filter(token -> !Set.of("tutorial", "guide", "course", "lecture", "learn", "beginner").contains(token))
                .filter(token -> token.length() > 2)
                .toList();
    }

    private int countMatches(String normalizedTitle, String normalizedDescription, List<String> significantTerms) {
        int matches = 0;
        for (String term : significantTerms) {
            if (normalizedTitle.contains(term) || normalizedDescription.contains(term)) {
                matches++;
            }
        }
        return matches;
    }

    private boolean containsAny(String title, String description, String channel, Set<String> keywords) {
        for (String keyword : keywords) {
            if (title.contains(keyword) || description.contains(keyword) || channel.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}

