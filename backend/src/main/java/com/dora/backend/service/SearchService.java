package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import java.util.ArrayList;
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
        String normalizedQuery = normalize(query);
        String normalizedSource = normalize(source);

        if (!isPresent(normalizedQuery)) {
            return List.of();
        }

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;

        List<Document> results = new ArrayList<>(
                documentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        normalizedQuery,
                        normalizedQuery));

        results.addAll(youTubeApiService.searchYouTube(normalizedQuery));
        results.addAll(gitHubApiService.searchRepositories(normalizedQuery));

        List<Document> rankedResults = rankAndSort(results, normalizedQuery);
        List<Document> filteredResults = applySourceFilter(rankedResults, normalizedSource);

        logger.info(
                "Search request | query='{}' | page={} | size={} | totalResults={} | source='{}'",
                normalizedQuery,
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

    private List<Document> rankAndSort(List<Document> results, String query) {
        for (Document document : results) {
            double baseScore = "github".equalsIgnoreCase(document.getSource()) && document.getScore() != null
                    ? document.getScore()
                    : 0.0;
            double score = baseScore + rankingService.calculateScore(document, query);
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

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
