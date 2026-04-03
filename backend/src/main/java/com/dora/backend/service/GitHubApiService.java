package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GitHubApiService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubApiService.class);
    private static final String GITHUB_SEARCH_URL = "https://api.github.com/search/repositories";
    private static final Set<String> NEGATIVE_REPO_KEYWORDS = Set.of(
            "bootcamp",
            "workshop",
            "assignment",
            "assignement",
            "homework",
            "attendance",
            "quiz",
            "lab",
            "practice",
            "readme");
    private static final Set<String> POSITIVE_REPO_KEYWORDS = Set.of(
            "tutorial",
            "course",
            "guide",
            "book",
            "notes",
            "ml",
            "ai",
            "deep learning",
            "machine learning",
            "data structures",
            "algorithms",
            "operating systems");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String githubApiKey;

    public GitHubApiService(ObjectMapper objectMapper, @Value("${github.api.key:}") String githubApiKey) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = objectMapper;
        this.githubApiKey = githubApiKey;
    }

    public List<Document> searchRepositories(String query) {
        if (query == null || query.trim().isEmpty() || githubApiKey == null || githubApiKey.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(GITHUB_SEARCH_URL)
                    .queryParam("q", query.trim())
                    .queryParam("per_page", 30)
                    .toUriString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + githubApiKey)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "DORA-Academic-Search")
                    .GET()
                    .build();

            logger.info("Calling GitHub Search API for query='{}'", query);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warn(
                        "GitHub Search API returned status {} for query='{}' with body preview: {}",
                        response.statusCode(),
                        query,
                        previewBody(response.body()));
                return Collections.emptyList();
            }

            String body = response.body();
            if (body == null || body.isBlank()) {
                logger.warn("GitHub Search API returned an empty body for query='{}'", query);
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.path("items");
            if (!items.isArray()) {
                logger.warn(
                        "GitHub Search API returned a body without an items array for query='{}': {}",
                        query,
                        previewBody(body));
                return Collections.emptyList();
            }

            List<Document> documents = new ArrayList<>();
            int currentYear = Year.now().getValue();
            List<String> significantTerms = extractSignificantTerms(query);

            for (JsonNode item : items) {
                String name = item.path("name").asText();
                String fullName = item.path("full_name").asText(name);
                String htmlUrl = item.path("html_url").asText();
                String description = item.path("description").asText("");
                int stars = item.path("stargazers_count").asInt(0);
                int forks = item.path("forks_count").asInt(0);

                if (htmlUrl == null || htmlUrl.isBlank()) {
                    continue;
                }

                if (!isRelevantRepository(name, description, significantTerms, stars, forks)) {
                    continue;
                }

                Document document = new Document();
                document.setTitle(fullName);
                document.setUrl(htmlUrl);
                document.setDescription(cleanText(description));
                document.setSource("github");
                document.setType("repository");
                document.setYear(currentYear);
                document.setScore(calculateScore(name, description, significantTerms, stars, forks));
                documents.add(document);
            }

            logger.info(
                    "GitHub Search API raw items={} filtered repositories={} for query='{}'",
                    items.size(),
                    documents.size(),
                    query);
            return documents;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error("GitHub search interrupted for query='{}'", query, ex);
            return Collections.emptyList();
        } catch (Exception ex) {
            logger.error("Error while fetching GitHub results for query='{}'", query, ex);
            return Collections.emptyList();
        }
    }

    private boolean isRelevantRepository(
            String name,
            String description,
            List<String> significantTerms,
            int stars,
            int forks) {
        String normalizedName = normalize(name);
        String normalizedDescription = normalize(description);
        int matchedTerms = countMatches(normalizedName, normalizedDescription, significantTerms);

        if (matchedTerms == 0) {
            return false;
        }

        boolean hasNegativeSignal = containsAny(normalizedName, normalizedDescription, NEGATIVE_REPO_KEYWORDS);
        boolean hasPositiveSignal = containsAny(normalizedName, normalizedDescription, POSITIVE_REPO_KEYWORDS);

        if (matchedTerms == 1 && (hasPositiveSignal || stars >= 20 || forks >= 8)) {
            return true;
        }

        if (stars >= 150 || forks >= 40) {
            return true;
        }

        if (significantTerms.size() > 1
                && matchedTerms < Math.min(2, significantTerms.size())
                && !hasPositiveSignal
                && stars < 150
                && forks < 40) {
            return false;
        }

        if (hasNegativeSignal && !hasPositiveSignal && stars < 100 && forks < 30) {
            return false;
        }

        return !(normalizedName.equals("readme") || normalizedName.equals("readme.md"));
    }

    private double calculateScore(
            String name,
            String description,
            List<String> significantTerms,
            int stars,
            int forks) {
        String normalizedName = normalize(name);
        String normalizedDescription = normalize(description);
        int matchedTerms = countMatches(normalizedName, normalizedDescription, significantTerms);

        double score = 35.0;
        score += matchedTerms * 10.0;
        score += Math.min(25.0, Math.log10(stars + 1) * 12.0);
        score += Math.min(10.0, Math.log10(forks + 1) * 6.0);

        if (containsAny(normalizedName, normalizedDescription, POSITIVE_REPO_KEYWORDS)) {
            score += 12.0;
        }

        if (containsAny(normalizedName, normalizedDescription, NEGATIVE_REPO_KEYWORDS)) {
            score -= 10.0;
        }

        return Math.max(score, 20.0);
    }

    private List<String> extractSignificantTerms(String query) {
        String sanitized = query == null ? "" : query.toLowerCase(Locale.ROOT)
                .replaceAll("\\bin:[^\\s]+", " ")
                .replaceAll("\\bfork:false\\b", " ")
                .replaceAll("\\barchived:false\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return java.util.Arrays.stream(sanitized.split(" "))
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() > 2)
                .toList();
    }

    private int countMatches(String normalizedName, String normalizedDescription, List<String> significantTerms) {
        int matches = 0;
        for (String term : significantTerms) {
            if (normalizedName.contains(term) || normalizedDescription.contains(term)) {
                matches++;
            }
        }
        return matches;
    }

    private boolean containsAny(String normalizedName, String normalizedDescription, Set<String> keywords) {
        for (String keyword : keywords) {
            if (normalizedName.contains(keyword) || normalizedDescription.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private String previewBody(String body) {
        if (body == null || body.isBlank()) {
            return "<empty>";
        }

        String compact = body.replaceAll("\\s+", " ").trim();
        return compact.substring(0, Math.min(300, compact.length()));
    }
}
