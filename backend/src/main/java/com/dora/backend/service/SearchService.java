package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentSpecifications;
import com.dora.backend.repository.DocumentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final DocumentRepository documentRepository;
    private final YouTubeApiService youTubeApiService;
    private final RankingService rankingService;
    private final QueryPreprocessorService queryPreprocessorService;

    public SearchService(
            DocumentRepository documentRepository,
            YouTubeApiService youTubeApiService,
            RankingService rankingService,
            QueryPreprocessorService queryPreprocessorService) {
        this.documentRepository = documentRepository;
        this.youTubeApiService = youTubeApiService;
        this.rankingService = rankingService;
        this.queryPreprocessorService = queryPreprocessorService;
    }

    public List<Document> search(String query, String source, int page, int size) {
        String normalizedQuery = normalize(query);
        String cleanedQuery = queryPreprocessorService.preprocess(normalizedQuery);
        List<String> keywords = queryPreprocessorService.extractKeywords(cleanedQuery);
        String normalizedSource = normalize(source);

        if (keywords.isEmpty()) {
            return List.of();
        }

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;

        List<Document> results = new ArrayList<>(documentRepository.findAll(
                DocumentSpecifications.hasAnyKeywordInTitleOrDescription(keywords)));

        results.addAll(youTubeApiService.searchYouTube(cleanedQuery));

        List<Document> rankedResults = rankAndSort(results, keywords);
        List<Document> filteredResults = applySourceFilter(rankedResults, normalizedSource);

        logger.info(
                "Search request | query='{}' | cleanedQuery='{}' | keywords={} | page={} | size={} | totalResults={} | source='{}'",
                normalizedQuery,
                cleanedQuery,
                keywords,
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

    private List<Document> rankAndSort(List<Document> results, List<String> keywords) {
        return results.stream()
                .peek(document -> {
                    double score = rankingService.calculateScore(document, keywords);
                    document.setScore(score);
                    logger.debug(
                            "Ranking result | keywords={} | source='{}' | title='{}' | score={}",
                            keywords,
                            document.getSource(),
                            document.getTitle(),
                            score);
                })
                .sorted(Comparator.comparingDouble(this::getScoreSafely).reversed())
                .collect(Collectors.toList());
    }

    private double getScoreSafely(Document document) {
        return document.getScore() == null ? 0.0 : document.getScore();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
