package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WikipediaService {

    private static final Logger logger = LoggerFactory.getLogger(WikipediaService.class);
    private static final String WIKIPEDIA_SEARCH_URL = "https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&srlimit=20";

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
            String url = WIKIPEDIA_SEARCH_URL + "&srsearch=" + encodedQuery;

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

            List<Document> results = parseWikipediaSearchResponse(response);
            logger.info("Wikipedia query='{}' found {} results", trimmedQuery, results.size());
            logger.info("Wikipedia size: {}", results.size());
            return results;
        } catch (Exception ex) {
            logger.error("Wikipedia query='{}' failed", trimmedQuery, ex);
            logger.info("Wikipedia size: 0");
            return Collections.emptyList();
        }
    }

    private List<Document> parseWikipediaSearchResponse(String response) throws Exception {
        List<Document> results = new ArrayList<>();
        JsonNode root = objectMapper.readTree(response);
        JsonNode searchResults = root.path("query").path("search");

        if (!searchResults.isArray()) {
            return results;
        }

        for (JsonNode item : searchResults) {
            String title = item.path("title").asText("").trim();
            String snippet = item.path("snippet").asText("").trim();
            int pageid = item.path("pageid").asInt(0);

            if (isBlank(title) || pageid == 0) {
                continue;
            }

            String cleaned_snippet = stripHtmlTags(snippet);
            String pageUrl = "https://en.wikipedia.org/?curid=" + pageid;

            Document article = new Document();
            article.setTitle(title);
            article.setDescription(cleaned_snippet);
            article.setUrl(pageUrl);
            article.setSource("wikipedia");
            article.setType("article");
            article.setScore(90.0);

            results.add(article);
        }

        return results;
    }

    private String stripHtmlTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}