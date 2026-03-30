package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final DocumentRepository documentRepository;
    private final YouTubeApiService youTubeApiService;
    private final RankingService rankingService;

    public SearchService(
            DocumentRepository documentRepository,
            YouTubeApiService youTubeApiService,
            RankingService rankingService) {
        this.documentRepository = documentRepository;
        this.youTubeApiService = youTubeApiService;
        this.rankingService = rankingService;
    }

    public List<Document> search(String query, String type, String source) {
        String normalizedQuery = normalize(query);
        String normalizedType = normalize(type);
        String normalizedSource = normalize(source);

        boolean hasQuery = isPresent(normalizedQuery);
        boolean hasType = isPresent(normalizedType);
        boolean hasSource = isPresent(normalizedSource);

        List<Document> results;

        if (!hasQuery && !hasType && !hasSource) {
            results = new ArrayList<>(documentRepository.findAll());
        } else if (hasQuery && !hasType && !hasSource) {
            results = new ArrayList<>(
                    documentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            normalizedQuery,
                            normalizedQuery));
        } else if (!hasQuery && hasType && !hasSource) {
            results = new ArrayList<>(documentRepository.findByTypeIgnoreCase(normalizedType));
        } else if (!hasQuery && !hasType && hasSource) {
            results = new ArrayList<>(documentRepository.findBySourceIgnoreCase(normalizedSource));
        } else {
            results = new ArrayList<>(
                    documentRepository.searchDocuments(normalizedQuery, normalizedType, normalizedSource));
        }

        if (hasQuery) {
            results.addAll(youTubeApiService.searchYouTube(normalizedQuery));
        }

        return rankAndSort(results, normalizedQuery);
    }

    private List<Document> rankAndSort(List<Document> results, String query) {
        for (Document document : results) {
            double score = rankingService.calculateScore(document, query);
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
