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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GitHubApiService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubApiService.class);
    private static final String GITHUB_SEARCH_URL = "https://api.github.com/search/repositories";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String githubApiKey;

    public GitHubApiService(ObjectMapper objectMapper, @Value("${github.api.key:}") String githubApiKey) {
        this.restTemplate = new RestTemplate();
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
                    .queryParam("per_page", 100)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubApiKey);
            headers.set("Accept", "application/vnd.github+json");

            logger.info("Calling GitHub Search API for query='{}'", query);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.path("items");
            if (!items.isArray()) {
                return Collections.emptyList();
            }

            List<Document> documents = new ArrayList<>();
            int currentYear = Year.now().getValue();

            for (JsonNode item : items) {
                String name = item.path("name").asText();
                String htmlUrl = item.path("html_url").asText();
                String description = item.path("description").asText("");
                int stars = item.path("stargazers_count").asInt(0);

                if (htmlUrl == null || htmlUrl.isBlank()) {
                    continue;
                }

                Document document = new Document();
                document.setTitle(name);
                document.setUrl(htmlUrl);
                document.setDescription(description);
                document.setSource("github");
                document.setType("repository");
                document.setYear(currentYear);
                document.setScore(80.0 + Math.min(stars, 10));
                documents.add(document);
            }

            logger.info("GitHub Search API returned {} repositories for query='{}'", documents.size(), query);
            return documents;
        } catch (Exception ex) {
            logger.error("Error while fetching GitHub results", ex);
            return Collections.emptyList();
        }
    }
}
