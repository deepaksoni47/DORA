package com.dora.backend.service;

import com.dora.backend.entity.Document;
import java.time.Year;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    public double calculateScore(Document doc, String query) {
        if (doc == null) {
            return 0.0;
        }

        double score = 0.0;
        String normalizedQuery = normalize(query);

        if (!normalizedQuery.isEmpty()) {
            if (containsIgnoreCase(doc.getTitle(), normalizedQuery)) {
                score += 10.0;
            }
            if (containsIgnoreCase(doc.getDescription(), normalizedQuery)) {
                score += 5.0;
            }
        }

        score += sourceWeight(doc);

        Integer year = doc.getYear();
        int currentYear = Year.now().getValue();
        if (year != null && year >= currentYear - 1) {
            score += 2.0;
        }

        return score;
    }

    private double sourceWeight(Document doc) {
        String source = normalize(doc.getSource());
        String type = normalize(doc.getType());

        if ("paper".equals(source) || "paper".equals(type)) {
            return 5.0;
        }
        if ("github".equals(source) || "github".equals(type)) {
            return 4.0;
        }
        if ("youtube".equals(source) || "youtube".equals(type)) {
            return 3.0;
        }
        if ("article".equals(source) || "article".equals(type)) {
            return 2.0;
        }

        return 0.0;
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
