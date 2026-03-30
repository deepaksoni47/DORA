package com.dora.backend.service;

import com.dora.backend.entity.Document;
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
        String normalizedTitle = normalize(doc.getTitle());
        String normalizedDescription = normalize(doc.getDescription());
        String normalizedSource = normalize(doc.getSource());

        if (!normalizedQuery.isEmpty()) {
            if (normalizedTitle.contains(normalizedQuery)) {
                score += 50.0;
            }

            if (normalizedTitle.startsWith(normalizedQuery)) {
                score += 30.0;
            }

            if (normalizedDescription.contains(normalizedQuery)) {
                score += 20.0;
            }
        }

        if ("youtube".equals(normalizedSource)) {
            score += 20.0;
        }

        if ("crawler".equals(normalizedSource)) {
            score += 10.0;
        }

        return score;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
