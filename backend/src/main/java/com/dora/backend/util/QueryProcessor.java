package com.dora.backend.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * QueryProcessor: Transforms natural language queries into optimized search
 * terms.
 *
 * Responsibilities:
 * - Remove common stop words (filler words that don't add meaning)
 * - Expand abbreviations into full terms (dsa → data structures algorithms)
 * - Detect learning intent and add relevant keywords (tutorial, beginner,
 * guide)
 * - Extract meaningful keywords for scoring
 *
 * This ensures queries like "i want to start dsa" become:
 * "data structures algorithms tutorial beginner"
 */
public class QueryProcessor {

    // Comprehensive set of common English words that don't add search value
    private static final Set<String> STOP_WORDS = buildStopWordsSet();

    // Map abbreviations and short terms to fuller, more searchable concepts
    // Format: abbreviation/shorthand → expanded concept(s)
    private static final Map<String, String> CONCEPT_EXPANSIONS = buildConceptMap();

    // Learning intent keywords that trigger addition of tutorial/guide keywords
    private static final Set<String> LEARNING_INTENTS = buildLearningIntents();

    private QueryProcessor() {
    }

    private static Map<String, String> buildConceptMap() {
        Map<String, String> map = new HashMap<>();
        map.put("dsa", "data structures algorithms");
        map.put("ds", "data structures");
        map.put("algo", "algorithms");
        map.put("ai", "artificial intelligence");
        map.put("ml", "machine learning");
        map.put("dl", "deep learning");
        map.put("nlp", "natural language processing");
        map.put("cv", "computer vision");
        map.put("dbms", "database systems");
        map.put("db", "database");
        map.put("sql", "sql database");
        map.put("nosql", "nosql database");
        map.put("os", "operating system");
        map.put("api", "api development");
        map.put("rest", "rest api");
        map.put("react", "react framework");
        map.put("js", "javascript");
        map.put("ts", "typescript");
        map.put("py", "python");
        map.put("java", "java programming");
        map.put("cpp", "c++ programming");
        map.put("oop", "object oriented programming");
        map.put("fp", "functional programming");
        map.put("git", "version control git");
        map.put("ci", "continuous integration");
        map.put("cd", "continuous deployment");
        map.put("devops", "devops practices");
        map.put("docker", "docker containerization");
        map.put("k8s", "kubernetes orchestration");
        map.put("aws", "amazon web services");
        map.put("gcp", "google cloud platform");
        map.put("azure", "microsoft azure");
        map.put("iot", "internet of things");
        map.put("blockchain", "blockchain technology");
        map.put("crypto", "cryptocurrency");
        return map;
    }

    private static Set<String> buildStopWordsSet() {
        Set<String> stopWords = new java.util.HashSet<>();
        // Pronouns
        stopWords.addAll(java.util.Arrays.asList(
                "i", "me", "my", "we", "our", "you", "your",
                "he", "him", "his", "she", "her", "it", "its"));
        // Question words
        stopWords.addAll(java.util.Arrays.asList(
                "what", "which", "who", "whom", "whose"));
        // Verbs and modals
        stopWords.addAll(java.util.Arrays.asList(
                "want", "like", "need", "get", "have", "has", "do", "does", "did",
                "would", "could", "should", "can", "will", "shall", "may", "might", "must",
                "is", "am", "are", "was", "were", "be", "been", "being"));
        // Articles, conjunctions, negations
        stopWords.addAll(java.util.Arrays.asList(
                "the", "a", "an", "and", "or", "but", "not", "no"));
        // Prepositions
        stopWords.addAll(java.util.Arrays.asList(
                "in", "on", "at", "by", "for", "of", "to", "from", "with", "as"));
        // Adverbs and other common words
        stopWords.addAll(java.util.Arrays.asList(
                "about", "how", "where", "when", "why",
                "this", "that", "these", "those", "other", "another"));
        return stopWords;
    }

    private static Set<String> buildLearningIntents() {
        Set<String> intents = new java.util.HashSet<>();
        intents.addAll(java.util.Arrays.asList(
                "learn", "start", "begin", "understand", "study", "explore", "master",
                "how", "tutorial", "guide", "introduction", "basics"));
        return intents;
    }

    /**
     * Normalize a query by:
     * 1. Converting to lowercase
     * 2. Removing stop words
     * 3. Expanding abbreviations (dsa → data structures algorithms)
     * 4. Adding learning keywords if intent detected (tutorial, beginner, guide)
     *
     * Example:
     * Input: "i want to start dsa"
     * Output: "data structures algorithms tutorial beginner guide"
     */
    public static String normalize(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        String lower = query.toLowerCase(Locale.ROOT).trim();
        lower = lower.replaceAll("\\s+", " "); // normalize whitespace

        String[] words = lower.split("\\s+");
        List<String> expanded = new ArrayList<>();

        // Process each word: expand abbreviations, skip stop words, keep meaningful
        // terms
        for (String word : words) {
            if (word.isBlank() || STOP_WORDS.contains(word)) {
                continue;
            }

            // Check if this word should be expanded to a fuller concept
            String expansion = CONCEPT_EXPANSIONS.get(word);
            if (expansion != null) {
                expanded.add(expansion);
            } else {
                // Keep the meaningful word as-is
                expanded.add(word);
            }
        }

        // Detect learning intent and add pedagogical keywords
        if (hasLearningIntent(lower)) {
            expanded.add("tutorial");
            expanded.add("beginner");
            expanded.add("guide");
        }

        return String.join(" ", expanded).trim();
    }

    /**
     * Extract meaningful keywords from a query.
     * Useful for debugging or analyzing search intent.
     */
    public static List<String> extractKeywords(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String lower = query.toLowerCase(Locale.ROOT).trim();
        String[] words = lower.split("\\s+");
        List<String> keywords = new ArrayList<>();

        for (String word : words) {
            if (!word.isBlank() && !STOP_WORDS.contains(word)) {
                keywords.add(word);
            }
        }

        return keywords;
    }

    /**
     * Check if query contains learning-related intent.
     */
    private static boolean hasLearningIntent(String lowerQuery) {
        for (String intent : LEARNING_INTENTS) {
            if (lowerQuery.contains(intent)) {
                return true;
            }
        }
        return false;
    }
}
