package com.dora.backend.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class QueryNormalizer {

    private static final Set<String> STOPWORDS = Set.of(
            "i",
            "want",
            "to",
            "how",
            "can",
            "should",
            "start",
            "learn",
            "begin",
            "please");

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "dsa", "data structures",
            "ml", "machine learning",
            "ai", "artificial intelligence",
            "api", "api development",
            "db", "database");

    private static final Set<String> INTENT_KEYWORDS = Set.of(
            "tutorial",
            "course",
            "guide",
            "learn");

    public String normalize(String query) {
        if (query == null) {
            return "";
        }

        String normalized = query.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replaceAll("[^a-zA-Z0-9 ]", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        if (normalized.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String token : normalized.split(" ")) {
            if (token.isBlank() || STOPWORDS.contains(token)) {
                continue;
            }

            String replacement = ABBREVIATIONS.get(token);
            if (replacement != null) {
                builder.append(replacement);
            } else {
                builder.append(token);
            }
            builder.append(' ');
        }

        normalized = builder.toString().trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }

        boolean hasIntentKeyword = Arrays.stream(normalized.split(" "))
                .anyMatch(INTENT_KEYWORDS::contains);

        if (!hasIntentKeyword) {
            normalized = normalized + " tutorial";
        }

        return normalized;
    }
}
