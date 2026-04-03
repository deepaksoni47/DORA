package com.dora.backend.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides safe and deterministic query normalization for search operations.
 */
public final class QueryProcessor {

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

    private static final Set<String> WEAK_STOPWORDS = Set.of(
            "i", "me", "my", "we", "you", "your", "he", "she", "it",
            "a", "an", "the",
            "is", "am", "are", "was", "were",
            "to", "for", "of", "in", "on", "at",
            "can", "could", "should", "would",
            "please");

    private static final Set<String> PRESERVED_PHRASES = Set.of(
            "data structures",
            "machine learning",
            "operating system",
            "artificial intelligence");

    private static final Set<String> IMPORTANT_KEYWORDS = Set.of(
            "data", "structures", "algorithms", "dsa",
            "machine", "learning", "ai", "ml",
            "operating", "system", "os",
            "database", "dbms",
            "python", "java", "c++",
            "tutorial", "course", "guide", "beginner", "advanced");

    private static final Set<String> SECONDARY_INTENT_KEYWORDS = Set.of(
            "learn", "study", "tutorial", "course", "guide", "beginner", "beginners", "advanced");

    private static final Set<String> SEARCH_QUALIFIER_KEYWORDS = Set.of(
            "tutorial", "course", "guide", "beginner", "advanced");

    private static final Map<String, String> QUERY_EXPANSIONS = Map.of(
            "dsa", "data structures algorithms",
            "os", "operating system",
            "dbms", "database management system",
            "ai", "artificial intelligence",
            "ml", "machine learning");

    private QueryProcessor() {
    }

    public static String normalizeQuery(String query) {
        if (query == null) {
            logger.info("Original: null");
            logger.info("Normalized: ");
            return "";
        }

        String trimmedQuery = query.trim();
        if (shouldPreserveMeaningfulQuery(trimmedQuery)) {
            String preserved = trimmedQuery.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
            logger.info("Original: \"{}\"", query);
            logger.info("Normalized: \"{}\"", preserved);
            return preserved;
        }

        String originalLower = query.toLowerCase(Locale.ROOT);
        String normalizedInput = originalLower.replaceAll("\\b(i|we)\\s+want\\s+to\\b", "$1 to");

        // Preserve important technical tokens with special characters before cleaning
        String safeInput = normalizedInput
                .replaceAll("\\bc\\+\\+\\b", "cplusplus")
                .replaceAll("\\bc#\\b", "csharp")
                .replaceAll("\\bf#\\b", "fsharp")
                .replaceAll("\\bnode\\.js\\b", "nodejs")
                .replaceAll("\\basp\\.net\\b", "aspnet");

        String normalized = safeInput
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String cleaned = java.util.Arrays.stream(normalized.split(" "))
                .filter(token -> !token.isBlank())
                .filter(token -> !WEAK_STOPWORDS.contains(token))
                .collect(Collectors.joining(" "));

        if (cleaned.isBlank()) {
            cleaned = originalLower.trim().replaceAll("\\s+", " ");
        }

        String result = ensurePreservedPhrases(cleaned);

        logger.info("Original: \"{}\"", query);
        logger.info("Normalized: \"{}\"", result);
        return result;
    }

    private static boolean shouldPreserveMeaningfulQuery(String query) {
        if (query == null || query.isBlank()) {
            return false;
        }

        String compact = query.replaceAll("\\s+", " ").trim();
        if (!compact.matches("[a-zA-Z ]+")) {
            return false;
        }

        String[] words = compact.split(" ");
        if (words.length < 2) {
            return false;
        }

        for (String word : words) {
            if (!word.isBlank() && WEAK_STOPWORDS.contains(word.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }

        return true;
    }

    public static List<String> extractKeywords(String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return List.of();
        }

        String compact = normalizedQuery.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
        String[] words = compact.split(" ");

        LinkedHashSet<String> primaryKeywords = new LinkedHashSet<>();
        LinkedHashSet<String> secondaryKeywords = new LinkedHashSet<>();
        LinkedHashSet<String> extraKeywords = new LinkedHashSet<>();

        for (int i = 0; i < words.length; i++) {
            if (words[i].isBlank()) {
                continue;
            }

            if (i < words.length - 1) {
                String phrase = words[i] + " " + words[i + 1];
                if (PRESERVED_PHRASES.contains(phrase)) {
                    primaryKeywords.add(phrase);
                    i++;
                    continue;
                }
            }

            String token = normalizeToken(words[i]);
            if (token.isBlank()) {
                continue;
            }

            if (isPrimaryKeyword(token)) {
                primaryKeywords.add(token);
            } else if (SECONDARY_INTENT_KEYWORDS.contains(token)) {
                secondaryKeywords.add(token);
            } else if (!WEAK_STOPWORDS.contains(token)) {
                extraKeywords.add(token);
            }
        }

        List<String> combined = new ArrayList<>(
                primaryKeywords.size() + secondaryKeywords.size() + extraKeywords.size());
        combined.addAll(primaryKeywords);
        combined.addAll(secondaryKeywords);
        combined.addAll(extraKeywords);
        return combined;
    }

    public static String buildSearchQuery(String normalizedQuery) {
        String base = normalizedQuery == null ? "" : normalizedQuery.replaceAll("\\s+", " ").trim();
        // Expand ambiguous queries before keyword extraction
        if (QUERY_EXPANSIONS.containsKey(base)) {
            base = QUERY_EXPANSIONS.get(base);
        }
        if (base.isBlank()) {
            logger.info("Normalized: \"{}\"", base);
            logger.info("Keywords: []");
            logger.info("Final Query: \"\"");
            return "";
        }

        List<String> keywords = extractKeywords(base);
        List<String> primary = keywords.stream()
                .map(QueryProcessor::normalizeToken)
                .filter(QueryProcessor::isPrimaryKeyword)
                .collect(Collectors.toList());

        if (primary.isEmpty()) {
            logger.info("Normalized: \"{}\"", base);
            logger.info("Keywords: {}", keywords);
            logger.info("Final Query: \"{}\"", base);
            return base;
        }

        LinkedHashSet<String> finalKeywords = new LinkedHashSet<>(primary);

        for (String keyword : keywords) {
            String token = normalizeToken(keyword);
            if (SEARCH_QUALIFIER_KEYWORDS.contains(token)) {
                finalKeywords.add(token);
            }
        }

        boolean hasGuideToken = finalKeywords.contains("tutorial")
                || finalKeywords.contains("course")
                || finalKeywords.contains("guide");
        if (!hasGuideToken) {
            finalKeywords.add("tutorial");
        }

        String finalQuery = String.join(" ", finalKeywords);

        logger.info("Normalized: \"{}\"", base);
        logger.info("Keywords: {}", keywords);
        logger.info("Final Query: \"{}\"", finalQuery);
        return finalQuery;
    }

    private static String ensurePreservedPhrases(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String normalizedValue = value.replaceAll("\\s+", " ").trim();
        for (String phrase : PRESERVED_PHRASES) {
            if (normalizedValue.contains(phrase)) {
                return normalizedValue;
            }
        }

        return normalizedValue;
    }

    private static String normalizeToken(String token) {
        if (token == null) {
            return "";
        }

        String normalized = token.trim().toLowerCase(Locale.ROOT);
        if ("beginners".equals(normalized)) {
            return "beginner";
        }
        return normalized;
    }

    private static boolean isPrimaryKeyword(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        if (PRESERVED_PHRASES.contains(token)) {
            return true;
        }

        if (!IMPORTANT_KEYWORDS.contains(token)) {
            return false;
        }

        return !SEARCH_QUALIFIER_KEYWORDS.contains(token);
    }
}
