package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class WikipediaService {

    private static final Logger logger = LoggerFactory.getLogger(WikipediaService.class);
    private static final String WIKIPEDIA_SUMMARY_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WikipediaService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public List<Document> searchConcept(String query) {
        if (query == null || query.trim().isEmpty()) {
            logger.info("Wikipedia size: 0");
            return Collections.emptyList();
        }

        String trimmedQuery = query.trim();
        try {
            String encodedQuery = URLEncoder.encode(trimmedQuery, StandardCharsets.UTF_8);
            String url = WIKIPEDIA_SUMMARY_URL + encodedQuery;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "DORA-SearchEngine/1.0 (https://github.com/dora)");

            ResponseEntity<String> apiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            String response = apiResponse.getBody();
            if (response == null || response.isBlank()) {
                logger.info("Wikipedia query='{}' failed: empty response", trimmedQuery);
                logger.info("Wikipedia size: 0");
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(response);
            String title = cleanText(root.path("title").asText(""));
            String description = cleanText(root.path("extract").asText(""));
            String pageUrl = cleanText(root.path("content_urls").path("desktop").path("page").asText(""));
            if (isBlank(pageUrl)) {
                pageUrl = cleanText(root.path("content_urls").path("mobile").path("page").asText(""));
            }

            if (isBlank(title) || isBlank(pageUrl)) {
                logger.info("Wikipedia query='{}' failed: page not found", trimmedQuery);
                logger.info("Wikipedia size: 0");
                return Collections.emptyList();
            }

            Document article = new Document();
            article.setTitle(title);
            article.setDescription(description);
            article.setUrl(pageUrl);
            article.setSource("wikipedia");
            article.setType("article");
            article.setScore(60.0);

            logger.info("Wikipedia query='{}' succeeded", trimmedQuery);
            List<Document> results = List.of(article);
            logger.info("Wikipedia size: {}", results.size());
            return results;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.info("Wikipedia query='{}' failed: page not found", trimmedQuery);
                logger.info("Wikipedia size: 0");
                return Collections.emptyList();
            }

            logger.error("Wikipedia query='{}' failed with status {}", trimmedQuery, ex.getStatusCode(), ex);
            logger.info("Wikipedia size: 0");
            return Collections.emptyList();
        } catch (Exception ex) {
            logger.error("Wikipedia query='{}' failed", trimmedQuery, ex);
            logger.info("Wikipedia size: 0");
            return Collections.emptyList();
        }
    }

    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value.replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}