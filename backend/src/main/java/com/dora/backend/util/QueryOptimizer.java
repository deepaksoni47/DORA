package com.dora.backend.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QueryOptimizer {

    private static final Logger log = LoggerFactory.getLogger(QueryOptimizer.class);

    private static final Set<String> GENERIC_INTENT_WORDS = Set.of(
            "tutorial", "course", "guide", "beginner");

    private static final Map<String, String> QUERY_EXPANSIONS = Map.of(
            "dsa", "data structures algorithms",
            "os", "operating system",
            "dbms", "database management system",
            "ai", "artificial intelligence",
            "ml", "machine learning");

    public Map<String, String> buildApiQueries(String finalQuery) {
        String safeFinalQuery = finalQuery == null ? "" : finalQuery.replaceAll("\\s+", " ").trim();
        String baseQuery = extractBaseQuery(safeFinalQuery);

        Map<String, String> apiQueries = new HashMap<>();
        apiQueries.put("youtube", appendMissingTokens(baseQuery, "tutorial"));

        String githubQuery = buildGitHubQuery(baseQuery);
        apiQueries.put("github", githubQuery);

        // Wikipedia: expand if ambiguous, else use baseQuery
        String wikiQuery = baseQuery;
        if (QUERY_EXPANSIONS.containsKey(baseQuery)) {
            wikiQuery = QUERY_EXPANSIONS.get(baseQuery);
        }
        apiQueries.put("wikipedia", wikiQuery);

        String arxivQuery = baseQuery.trim();
        // Safely truncate very long queries to first 3 words for academic context
        String[] words = arxivQuery.split("\\s+");
        if (words.length > 4) {
            arxivQuery = String.join(" ", words[0], words[1], words[2]);
        }
        apiQueries.put("arxiv", arxivQuery);

        apiQueries.put("crawler", baseQuery);

        log.info("YouTube query: {}", apiQueries.get("youtube"));
        log.info("GitHub query: {}", apiQueries.get("github"));
        log.info("Wikipedia query: {}", apiQueries.get("wikipedia"));
        log.info("Arxiv query: {}", apiQueries.get("arxiv"));
        log.info("Crawler query: {}", apiQueries.get("crawler"));

        return apiQueries;
    }

    private String extractBaseQuery(String finalQuery) {
        if (finalQuery == null || finalQuery.isBlank()) {
            return "";
        }

        String base = java.util.Arrays.stream(finalQuery.split(" "))
                .filter(token -> !token.isBlank())
                .filter(token -> !GENERIC_INTENT_WORDS.contains(token))
                .collect(Collectors.joining(" "));

        return base.isBlank() ? finalQuery : base;
    }

    private String buildGitHubQuery(String baseQuery) {
        if (baseQuery == null || baseQuery.isBlank()) {
            return "";
        }

        return baseQuery.trim() + " fork:false archived:false";
    }

    private String appendMissingTokens(String baseQuery, String... suffixTokens) {
        String value = baseQuery == null ? "" : baseQuery.trim();
        for (String suffixToken : suffixTokens) {
            if (suffixToken == null || suffixToken.isBlank()) {
                continue;
            }

            if (!value.contains(suffixToken)) {
                value = value.isBlank() ? suffixToken : value + " " + suffixToken;
            }
        }

        return value;
    }
}


