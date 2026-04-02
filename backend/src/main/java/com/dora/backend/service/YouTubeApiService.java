package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                    .queryParam("maxResults", 10)
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

            for (JsonNode item : items) {
                String videoId = item.path("id").path("videoId").asText();
                String title = item.path("snippet").path("title").asText();
                String description = item.path("snippet").path("description").asText();

                if (videoId == null || videoId.isBlank()) {
                    continue;
                }

                Document document = new Document();
                document.setTitle(title);
                document.setDescription(description);
                document.setUrl("https://www.youtube.com/watch?v=" + videoId);
                document.setSource("youtube");
                document.setType("video");
                document.setYear(currentYear);
                document.setScore(0.0);
                documents.add(document);
            }

            return documents;
        } catch (Exception ex) {
            logger.error("Error while fetching YouTube results", ex);
            return Collections.emptyList();
        }
    }
}
