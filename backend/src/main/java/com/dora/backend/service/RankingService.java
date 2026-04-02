package com.dora.backend.service;

import com.dora.backend.entity.Document;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * RankingService: Calculates relevance scores for search results.
 *
 * Scoring logic:
 * - Query term in title: +50 points (primary relevance indicator)
 * - Query term at title start: +30 additional points (strong signal)
 * - Query term in description: +20 points (supporting relevance)
 * - YouTube source: +20 points (quality video content)
 * - Crawler source: +10 points (web content)
 * - Database source: +15 points (curated content)
 *
 * This ensures that local database results and relevance-matched results
 * are preferred over raw API results when queries match well.
 */
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

        // Title-based scoring (strongest signal for relevance)
        if (!normalizedQuery.isEmpty()) {
            // Split query into tokens for more granular matching
            String[] queryTokens = normalizedQuery.split("\\s+");
            int titleMatches = 0;

            for (String token : queryTokens) {
                if (normalizedTitle.contains(token)) {
                    score += 25.0; // Each token match in title adds points
                    titleMatches++;
                }
            }

            // Bonus if all query tokens appear in title
            if (titleMatches == queryTokens.length && titleMatches > 0) {
                score += 30.0;
            }

            // Check if title starts with query (very strong signal)
            if (normalizedTitle.startsWith(normalizedQuery) ||
                    (queryTokens.length > 0 && normalizedTitle.startsWith(queryTokens[0]))) {
                score += 30.0;
            }

            // Description-based scoring (secondary signal)
            if (normalizedDescription.contains(normalizedQuery)) {
                score += 20.0;
            } else {
                // Partial credit for description containing some tokens
                for (String token : queryTokens) {
                    if (normalizedDescription.contains(token)) {
                        score += 8.0;
                    }
                }
            }
        }

        // Source-based scoring (ensure quality content types are boosted)
        if ("youtube".equalsIgnoreCase(normalizedSource)) {
            score += 20.0; // Video tutorials are valuable
        } else if ("crawler".equalsIgnoreCase(normalizedSource)) {
            score += 10.0; // Web crawled content has moderate value
        } else if ("database".equalsIgnoreCase(normalizedSource) ||
                normalizedSource.isEmpty()) {
            score += 15.0; // Local database content is curated
        } else if ("github".equalsIgnoreCase(normalizedSource)) {
            // GitHub scoring handled by SearchService using star count
            // Additional relevance boost if matched well
            if (!normalizedQuery.isEmpty()) {
                score += 5.0;
            }
        }

        return Math.max(score, 0.0); // Ensure non-negative scores
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
