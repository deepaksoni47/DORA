package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import com.dora.backend.util.QueryOptimizer;
import com.dora.backend.util.QueryProcessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final DocumentRepository documentRepository;
    private final YouTubeApiService youTubeApiService;
    private final GitHubApiService gitHubApiService;
    private final ArxivApiService arxivApiService;
    private final WikipediaService wikipediaService;
    private final RankingService rankingService;
    private final QueryOptimizer queryOptimizer;

    public SearchService(
            DocumentRepository documentRepository,
            YouTubeApiService youTubeApiService,
            GitHubApiService gitHubApiService,
            ArxivApiService arxivApiService,
            WikipediaService wikipediaService,
            RankingService rankingService,
            QueryOptimizer queryOptimizer) {
        this.documentRepository = documentRepository;
        this.youTubeApiService = youTubeApiService;
        this.gitHubApiService = gitHubApiService;
        this.arxivApiService = arxivApiService;
        this.wikipediaService = wikipediaService;
        this.rankingService = rankingService;
        this.queryOptimizer = queryOptimizer;
    }

    public List<Document> search(String query, String source, int page, int size) {
        String normalizedQuery = normalizeQuery(query);
        String finalQuery = QueryProcessor.buildSearchQuery(normalizedQuery);
        String normalizedSource = normalize(source);
        String[] words = tokenize(finalQuery);

        logger.info("Normalized query: {}", normalizedQuery);
        logger.info("Final query: {}", finalQuery);

        if (!isPresent(finalQuery)) {
            return List.of();
        }

        Map<String, String> apiQueries = queryOptimizer.buildApiQueries(finalQuery);

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;

        CompletableFuture<List<Document>> youtubeFuture = searchAsync(
                () -> youTubeApiService.searchYouTube(apiQueries.get("youtube")));
        CompletableFuture<List<Document>> githubFuture = searchAsync(
                () -> gitHubApiService.searchRepositories(apiQueries.get("github")));
        CompletableFuture<List<Document>> crawlerFuture = searchAsync(
                () -> fetchCrawlerResults(apiQueries.get("crawler")));
        CompletableFuture<List<Document>> wikipediaFuture = searchAsync(
                () -> wikipediaService.searchConcept(apiQueries.get("wikipedia")));
        CompletableFuture<List<Document>> arxivFuture = searchAsync(
                () -> arxivApiService.searchPapers(apiQueries.get("arxiv")));

        CompletableFuture.allOf(
                youtubeFuture,
                githubFuture,
                crawlerFuture,
                wikipediaFuture,
                arxivFuture).join();

        List<Document> youtubeResults = youtubeFuture.join();
        List<Document> githubResults = githubFuture.join();
        List<Document> crawlerResults = crawlerFuture.join();
        List<Document> wikipediaResults = wikipediaFuture.join();
        List<Document> arxivResults = arxivFuture.join();

        logger.info("YouTube: {}", youtubeResults.size());
        logger.info("GitHub: {}", githubResults.size());
        logger.info("Crawler: {}", crawlerResults.size());
        logger.info("Wikipedia: {}", wikipediaResults.size());
        logger.info("Arxiv: {}", arxivResults.size());
        logger.debug("Wikipedia results before merge: {}", wikipediaResults);
        logger.debug("Arxiv results before merge: {}", arxivResults);

        List<Document> allResults = new ArrayList<>();
        allResults.addAll(crawlerResults);
        allResults.addAll(youtubeResults);
        allResults.addAll(githubResults);
        allResults.addAll(wikipediaResults);
        allResults.addAll(arxivResults);

        logger.info("Total merged: {}", allResults.size());

        // Deduplicate results across all sources, keeping higher-scored duplicates
        Map<String, Document> uniqueResults = new LinkedHashMap<>();
        for (Document doc : allResults) {
            String key;

            // Prefer URL as unique key
            if (doc.getUrl() != null && !doc.getUrl().isBlank()) {
                key = doc.getUrl().trim().toLowerCase();
            } else {
                // Fallback to normalized title if URL unavailable
                String title = doc.getTitle() == null ? "" : doc.getTitle().trim();
                key = title.toLowerCase().replaceAll("\\s+", " ");
            }

            // If duplicate exists, keep the one with higher score
            if (uniqueResults.containsKey(key)) {
                Document existing = uniqueResults.get(key);
                Double existingScore = existing.getScore() == null ? 0.0 : existing.getScore();
                Double docScore = doc.getScore() == null ? 0.0 : doc.getScore();

                if (docScore > existingScore) {
                    uniqueResults.put(key, doc);
                }
            } else {
                uniqueResults.put(key, doc);
            }
        }

        List<Document> deduplicatedResults = new ArrayList<>(uniqueResults.values());
        logger.info("After deduplication: {}", deduplicatedResults.size());

        applyFallbackSearch(query, finalQuery, apiQueries.get("crawler"), deduplicatedResults);

        List<Document> rankedResults = rankAndSort(deduplicatedResults, finalQuery, words);
        List<Document> diverseResults = applySourceDiversity(rankedResults, 5, 20);
        List<Document> filteredResults = applySourceFilter(diverseResults, normalizedSource);

        logger.info(
                "Search request | query='{}' | page={} | size={} | totalResults={} | source='{}'",
                finalQuery,
                safePage,
                safeSize,
                filteredResults.size(),
                normalizedSource);

        List<Document> paginatedResults = paginate(filteredResults, safePage, safeSize);
        logger.info("Returned: {}", paginatedResults.size());
        return paginatedResults;
    }

    private double getScoreValue(Document document) {
        return document == null || document.getScore() == null ? 0.0 : document.getScore();
    }

    private void applyFallbackSearch(String rawQuery, String finalQuery, String baseQuery, List<Document> allResults) {
        if (!isWeakResults(allResults)) {
            return;
        }

        logger.info("Fallback triggered for query: {}", finalQuery);
        Set<String> seenKeys = allResults.stream()
                .map(this::buildResultKey)
                .collect(Collectors.toCollection(HashSet::new));

        String safeBaseQuery = baseQuery == null ? "" : baseQuery.trim();
        if (isPresent(safeBaseQuery) && isWeakResults(allResults)) {
            logger.info("Fallback level used: 1");
            addUniqueResults(allResults, fetchAllSourcesWithSingleQuery(safeBaseQuery), seenKeys);
        }

        if (isPresent(safeBaseQuery) && isWeakResults(allResults)) {
            logger.info("Fallback level used: 2");
            Set<String> keywords = new LinkedHashSet<>();
            for (String token : safeBaseQuery.split("\\s+")) {
                if (isPresent(token)) {
                    keywords.add(token.trim());
                }
            }

            for (String keyword : keywords) {
                addUniqueResults(allResults, fetchAllSourcesWithSingleQuery(keyword), seenKeys);
            }
        }

        String safeRawQuery = rawQuery == null ? "" : rawQuery.trim();
        if (allResults.isEmpty() && isPresent(safeRawQuery)) {
            logger.info("Fallback level used: 3");
            addUniqueResults(allResults, fetchAllSourcesWithSingleQuery(safeRawQuery), seenKeys);
        }
    }

    private List<Document> fetchAllSourcesWithSingleQuery(String query) {
        if (!isPresent(query)) {
            return Collections.emptyList();
        }
        CompletableFuture<List<Document>> youtubeFuture = searchAsync(() -> youTubeApiService.searchYouTube(query));
        CompletableFuture<List<Document>> githubFuture = searchAsync(() -> gitHubApiService.searchRepositories(query));
        CompletableFuture<List<Document>> crawlerFuture = searchAsync(() -> fetchCrawlerResults(query));
        CompletableFuture<List<Document>> wikipediaFuture = searchAsync(() -> wikipediaService.searchConcept(query));
        CompletableFuture<List<Document>> arxivFuture = searchAsync(() -> arxivApiService.searchPapers(query));

        CompletableFuture.allOf(
                youtubeFuture,
                githubFuture,
                crawlerFuture,
                wikipediaFuture,
                arxivFuture).join();

        List<Document> combined = new ArrayList<>();
        combined.addAll(crawlerFuture.join());
        combined.addAll(youtubeFuture.join());
        combined.addAll(githubFuture.join());
        combined.addAll(wikipediaFuture.join());
        combined.addAll(arxivFuture.join());
        return combined;
    }

    private CompletableFuture<List<Document>> searchAsync(Supplier<List<Document>> searchOperation) {
        return CompletableFuture.supplyAsync(() -> safeSearch(searchOperation))
                .completeOnTimeout(Collections.emptyList(), 5, TimeUnit.SECONDS);
    }

    private List<Document> safeSearch(Supplier<List<Document>> searchOperation) {
        try {
            return safeList(searchOperation.get());
        } catch (Exception ex) {
            logger.warn("Search source failed", ex);
            return Collections.emptyList();
        }
    }

    private void addUniqueResults(List<Document> target, List<Document> candidates, Set<String> seenKeys) {
        for (Document document : candidates) {
            String key = buildResultKey(document);
            if (seenKeys.add(key)) {
                target.add(document);
            }
        }
    }

    private String buildResultKey(Document document) {
        if (document == null) {
            return "";
        }

        String url = normalize(document.getUrl());
        if (isPresent(url)) {
            return url.toLowerCase();
        }

        String title = normalize(document.getTitle());
        String source = normalize(document.getSource());
        return (title == null ? "" : title.toLowerCase()) + "|" + (source == null ? "" : source.toLowerCase());
    }

    private boolean isWeakResults(List<Document> allResults) {
        return allResults.isEmpty() || allResults.size() < 5;
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
            return Collections.emptyList();
        }

        int end = Math.min(start + size, results.size());
        return results.subList(start, end);
    }

    private List<Document> fetchCrawlerResults(String normalizedQuery) {
        if (!isPresent(normalizedQuery)) {
            return Collections.emptyList();
        }

        return safeList(documentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                normalizedQuery,
                normalizedQuery));
    }

    private List<Document> safeList(List<Document> results) {
        return results == null ? Collections.emptyList() : results;
    }

    private List<Document> rankAndSort(List<Document> results, String query, String[] words) {
        // Filter out generic intent words to prevent ranking bias against
        // repositories/papers
        Set<String> ignoreWords = Set.of("tutorial", "guide", "course", "learn");
        String[] filteredWords = java.util.Arrays.stream(words)
                .filter(word -> !ignoreWords.contains(word.toLowerCase()))
                .toArray(String[]::new);

        for (Document document : results) {
            double baseScore = document.getScore() == null ? 0.0 : document.getScore();
            double score = baseScore + rankingService.calculateScore(document, query)
                    + calculateKeywordBoost(document, filteredWords);
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

    private List<Document> applySourceDiversity(List<Document> rankedResults, int maxPerSource, int topResultsLimit) {
        if (rankedResults.isEmpty()) {
            return rankedResults;
        }

        Map<String, Integer> sourceCount = new HashMap<>();
        List<Document> diverseResults = new ArrayList<>();
        Set<Document> addedDocs = new HashSet<>();

        // First pass: build top results with source diversity limit
        for (Document doc : rankedResults) {
            if (diverseResults.size() >= topResultsLimit) {
                break;
            }

            String source = doc.getSource() == null ? "unknown" : doc.getSource().toLowerCase();
            int count = sourceCount.getOrDefault(source, 0);

            if (count < maxPerSource) {
                diverseResults.add(doc);
                addedDocs.add(doc);
                sourceCount.put(source, count + 1);
            }
        }

        // Second pass: add remaining results without diversity constraint
        for (Document doc : rankedResults) {
            if (!addedDocs.contains(doc)) {
                diverseResults.add(doc);
            }
        }

        return diverseResults;
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
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizeQuery(String query) {
        return QueryProcessor.normalizeQuery(query);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
