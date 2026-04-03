package com.dora.backend.service;

import com.dora.backend.entity.Document;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private static final double TITLE_MATCH_WEIGHT = 2.0;
    private static final double DESCRIPTION_MATCH_WEIGHT = 1.0;

    public double calculateScore(Document doc, List<String> keywords) {
        if (doc == null || keywords == null || keywords.isEmpty()) {
            return 0.0;
        }

        String normalizedTitle = normalize(doc.getTitle());
        String normalizedDescription = normalize(doc.getDescription());

        return keywords.stream()
                .map(this::normalize)
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .mapToDouble(keyword -> scoreKeywordMatch(keyword, normalizedTitle, normalizedDescription))
                .sum();
    }

    public double calculateScore(Document doc, String query) {
        if (query == null || query.isBlank()) {
            return 0.0;
        }

        List<String> keywords = java.util.Arrays.stream(query.trim().split("\\s+"))
                .map(this::normalize)
                .filter(keyword -> !keyword.isEmpty())
                .collect(Collectors.toList());

        return calculateScore(doc, keywords);
    }

    private double scoreKeywordMatch(String keyword, String normalizedTitle, String normalizedDescription) {
        double score = 0.0;

        if (normalizedTitle.contains(keyword)) {
            score += TITLE_MATCH_WEIGHT;
        }

        if (normalizedDescription.contains(keyword)) {
            score += DESCRIPTION_MATCH_WEIGHT;
        }

        return score;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
