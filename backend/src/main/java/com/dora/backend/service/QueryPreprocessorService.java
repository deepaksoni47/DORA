package com.dora.backend.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class QueryPreprocessorService {

    private static final Set<String> STOP_WORDS = Set.of(
            "i",
            "want",
            "to",
            "learn",
            "how",
            "the",
            "is",
            "a",
            "an",
            "for",
            "of",
            "about");

    public String preprocess(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        return Arrays.stream(query.toLowerCase(Locale.ROOT).trim().split("\\s+"))
                .filter(token -> !token.isBlank())
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.joining(" "));
    }

    public List<String> extractKeywords(String cleanedQuery) {
        if (cleanedQuery == null || cleanedQuery.isBlank()) {
            return List.of();
        }

        return Arrays.stream(cleanedQuery.trim().split("\\s+"))
                .filter(token -> !token.isBlank())
                .toList();
    }
}
