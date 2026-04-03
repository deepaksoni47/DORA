package com.dora.backend.util;

import com.dora.backend.entity.Document;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class AcademicSubjectClassifier {

    private static final Map<String, Set<String>> SUBJECT_KEYWORDS = new LinkedHashMap<>();

    static {
        SUBJECT_KEYWORDS.put("computer_science", Set.of(
                "computer science", "programming", "software", "algorithm", "algorithms", "data structures",
                "operating systems", "database", "compiler", "machine learning", "artificial intelligence",
                "cybersecurity", "web development"));
        SUBJECT_KEYWORDS.put("medicine", Set.of(
                "medicine", "medical", "clinical", "disease", "anatomy", "physiology", "surgery", "pharmacology",
                "epidemiology", "public health", "patient", "healthcare"));
        SUBJECT_KEYWORDS.put("biology", Set.of(
                "biology", "genetics", "genomics", "cell", "molecular", "microbiology", "ecology", "botany",
                "zoology", "biochemistry", "neuroscience", "evolution"));
        SUBJECT_KEYWORDS.put("physics", Set.of(
                "physics", "quantum", "mechanics", "thermodynamics", "relativity", "astrophysics", "particle",
                "optics", "electromagnetism", "cosmology"));
        SUBJECT_KEYWORDS.put("chemistry", Set.of(
                "chemistry", "organic chemistry", "inorganic chemistry", "analytical chemistry", "chemical",
                "reaction", "stoichiometry", "spectroscopy"));
        SUBJECT_KEYWORDS.put("mathematics", Set.of(
                "mathematics", "math", "calculus", "algebra", "geometry", "probability", "statistics",
                "number theory", "topology", "linear algebra"));
        SUBJECT_KEYWORDS.put("economics", Set.of(
                "economics", "microeconomics", "macroeconomics", "econometrics", "finance", "accounting",
                "market", "trade", "banking"));
        SUBJECT_KEYWORDS.put("psychology", Set.of(
                "psychology", "cognitive", "behavior", "behaviour", "mental health", "therapy", "neuroscience",
                "developmental psychology", "social psychology"));
        SUBJECT_KEYWORDS.put("law", Set.of(
                "law", "legal", "jurisprudence", "constitution", "constitutional law", "criminal law",
                "contract law", "international law", "human rights"));
        SUBJECT_KEYWORDS.put("humanities", Set.of(
                "history", "philosophy", "literature", "linguistics", "ethics", "religion", "art history",
                "anthropology", "sociology", "political science"));
    }

    private AcademicSubjectClassifier() {}

    public static Set<String> detectSubjects(String query) {
        String normalized = normalize(query);
        Set<String> subjects = new LinkedHashSet<>();

        if (normalized.isBlank()) {
            subjects.add("general_academic");
            return subjects;
        }

        SUBJECT_KEYWORDS.forEach((subject, keywords) -> {
            boolean matched = keywords.stream().anyMatch(normalized::contains);
            if (matched) {
                subjects.add(subject);
            }
        });

        if (subjects.isEmpty()) {
            subjects.add("general_academic");
        }

        return subjects;
    }

    public static double calculateSourceWeight(Document document, Set<String> subjects) {
        String source = normalize(document == null ? null : document.getSource());

        if (source.isBlank()) {
            return 0.0;
        }

        double weight = 0.0;
        boolean csLike = subjects.contains("computer_science");
        boolean mathLike = subjects.contains("mathematics");
        boolean arxivLike = csLike || mathLike || subjects.contains("physics");
        boolean lifeScienceLike = subjects.contains("medicine") || subjects.contains("biology") || subjects.contains("chemistry");
        boolean socialScienceLike = subjects.contains("economics") || subjects.contains("psychology") || subjects.contains("law")
                || subjects.contains("humanities");

        switch (source) {
            case "github" -> {
                if (csLike) {
                    weight += 18.0;
                } else {
                    weight -= 28.0;
                }
            }
            case "arxiv" -> {
                if (arxivLike) {
                    weight += 18.0;
                } else if (lifeScienceLike || socialScienceLike) {
                    weight -= 10.0;
                }
            }
            case "crawler" -> {
                if (lifeScienceLike || socialScienceLike) {
                    weight += 15.0;
                } else {
                    weight += 8.0;
                }
            }
            case "wikipedia" -> {
                if (!subjects.contains("general_academic")) {
                    weight -= 6.0;
                }
            }
            case "youtube" -> {
                if (socialScienceLike || lifeScienceLike) {
                    weight -= 6.0;
                } else {
                    weight += 4.0;
                }
            }
            default -> {
            }
        }

        return weight;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
