package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import com.dora.backend.util.QueryProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final DocumentRepository documentRepository;
    private final YouTubeApiService youTubeApiService;
    private final GitHubApiService gitHubApiService;
    private final RankingService rankingService;

    public SearchService(
            DocumentRepository documentRepository,
            YouTubeApiService youTubeApiService,
            GitHubApiService gitHubApiService,
            RankingService rankingService) {
        this.documentRepository = documentRepository;
        this.youTubeApiService = youTubeApiService;
        this.gitHubApiService = gitHubApiService;
        this.rankingService = rankingService;
    }

    public List<Document> search(String query, String source, int page, int size) {
        String originalQuery = normalize(query);
        String normalizedQuery = QueryProcessor.normalize(query);
        String normalizedSource = normalize(source);
        String scoringQuery = isPresent(normalizedQuery) ? normalizedQuery : originalQuery;
        String[] words = tokenize(scoringQuery);

        logger.info("Original query: {}", originalQuery);
        logger.info("Normalized query: {}", normalizedQuery);

        if (!isPresent(originalQuery)) {
            return List.of();
        }

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;

        String apiQuery = isPresent(normalizedQuery) ? normalizedQuery : originalQuery;

        List<Document> dbResults = fetchDatabaseResults(normalizedQuery, originalQuery);
        List<Document> youtubeResults = safeList(youTubeApiService.searchYouTube(apiQuery));
        List<Document> githubResults = safeList(gitHubApiService.searchRepositories(apiQuery));

        logger.info(
                "Source result counts | originalQuery='{}' | normalizedQuery='{}' | db={} | youtube={} | github={}",
                originalQuery,
                normalizedQuery,
                dbResults.size(),
                youtubeResults.size(),
                githubResults.size());

        if (dbResults.isEmpty() && youtubeResults.isEmpty() && githubResults.isEmpty() && isPresent(originalQuery)) {
            youtubeResults = safeList(youTubeApiService.searchYouTube(originalQuery));
            logger.info(
                    "Fallback triggered | all sources empty using normalized query, retried YouTube with originalQuery='{}' | youtubeFallbackCount={}",
                    originalQuery,
                    youtubeResults.size());
        }

        List<Document> results = new ArrayList<>();
        results.addAll(dbResults);
        results.addAll(youtubeResults);
        results.addAll(githubResults);

        List<Document> rankedResults = rankAndSort(results, scoringQuery, words);
        List<Document> filteredResults = applySourceFilter(rankedResults, normalizedSource);

        logger.info(
                "Search request | query='{}' | page={} | size={} | totalResults={} | source='{}'",
                scoringQuery,
                safePage,
                safeSize,
                filteredResults.size(),
                normalizedSource);

        return paginate(filteredResults, safePage, safeSize);
    }

    private List<Document> applySourceFilter(List<Document> results, String source) {
        if (!isPresent(source)) {
            return results;
        }

        return results.stream()
                .filter(document -> isPresent(document.getSource()) && document.getSource().equalsIgnoreCase(source))
                .collect(Collectors.toList());
    }

    private List<Document> paginate(List<Document> results, int page, int size) {
        int start = page * size;
        if (start >= results.size()) {
            return List.of();
        }

        int end = Math.min(start + size, results.size());
        return results.subList(start, end);
    }

    private List<Document> fetchDatabaseResults(String normalizedQuery, String originalQuery) {
        List<Document> normalizedResults = isPresent(normalizedQuery)
                ? safeList(documentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        normalizedQuery,
                        normalizedQuery))
                : Collections.emptyList();

        if (!normalizedResults.isEmpty()) {
            return normalizedResults;
        }

        if (!isPresent(originalQuery) || originalQuery.equalsIgnoreCase(normalizedQuery)) {
            return normalizedResults;
        }

        return safeList(documentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                originalQuery,
                originalQuery));
    }

    private List<Document> safeList(List<Document> results) {
        return results == null ? Collections.emptyList() : results;
    }

    private List<Document> rankAndSort(List<Document> results, String query, String[] words) {
        for (Document document : results) {
            double baseScore = "github".equalsIgnoreCase(document.getSource()) && document.getScore() != null
                    ? document.getScore()
                    : 0.0;
            double score = baseScore + rankingService.calculateScore(document, query)
                    + calculateKeywordBoost(document, words);
            document.setScore(score);
            logger.debug(
                    "Ranking result | query='{}' | source='{}' | title='{}' | score={}",
                    query,
                    document.getSource(),
                    document.getTitle(),
                    score);
        }

        results.sort((a, b) -> Double.compare(
                b.getScore() == null ? 0.0 : b.getScore(),
                a.getScore() == null ? 0.0 : a.getScore()));
        return results;
    }

    private double calculateKeywordBoost(Document document, String[] words) {
        String title = normalize(document.getTitle());
        String description = normalize(document.getDescription());
        double boost = 0.0;
        int matchedWords = 0;

        // Boost for individual keyword matches
        for (String word : words) {
            if (!isPresent(word)) {
                continue;
            }

            boolean titleMatch = titleContainsWord(title, word);
            boolean descriptionMatch = descriptionContainsWord(description, word);

            if (titleMatch) {
                boost += 15.0; // Title match is highly relevant
                matchedWords++;
            }

            if (descriptionMatch) {
                boost += 8.0; // Description match is moderately relevant
                if (!titleMatch) {
                    matchedWords++;
                }
            }
        }

        // Bonus for matching all keywords (full query coverage)
        if (words.length > 0 && matchedWords == words.length) {
            boost += 20.0;
        }

        // Bonus for pedagogical keywords (tutorial, guide, beginner, course)
        if (containsAny(title, description, "beginner", "beginners", "tutorial", "course", "guide", "introduction",
                "basics")) {
            boost += 12.0;
        }

        // Penalty for no matching keywords (unlikely to be relevant)
        if (matchedWords == 0) {
            boost -= 10.0;
        }

        return Math.max(boost, 0.0); // Never go below 0
    }

    /**
     * Check if title contains word as a complete word (not substring).
     * "data structures" contains "structures" but not "struct".
     */
    private boolean titleContainsWord(String title, String word) {
        if (!isPresent(title) || !isPresent(word)) {
            return false;
        }
        String[] titleWords = title.split("\\s+");
        for (String titleWord : titleWords) {
            if (titleWord.equals(word) || titleWord.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if description contains word as a complete word (not substring).
     */
    private boolean descriptionContainsWord(String description, String word) {
        if (!isPresent(description) || !isPresent(word)) {
            return false;
        }
        String[] descWords = description.split("\\s+");
        for (String descWord : descWords) {
            if (descWord.equals(word) || descWord.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String title, String description, String... keywords) {
        for (String keyword : keywords) {
            if (title.contains(keyword) || description.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String[] tokenize(String normalizedQuery) {
        if (!isPresent(normalizedQuery)) {
            return new String[0];
        }

        return normalizedQuery.split(" ");
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
