package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

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

        if (!hasQuery && !hasType && !hasSource) {
            return rankAndSort(documentRepository.findAll(), normalizedQuery);
        }

        if (hasQuery && !hasType && !hasSource) {
            List<Document> dbResults = documentRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedQuery, normalizedQuery);
            return rankAndSort(combineWithYouTube(dbResults, normalizedQuery), normalizedQuery);
        }

        if (!hasQuery && hasType && !hasSource) {
            return rankAndSort(documentRepository.findByTypeIgnoreCase(normalizedType), normalizedQuery);
        }

        if (!hasQuery && !hasType && hasSource) {
            return rankAndSort(documentRepository.findBySourceIgnoreCase(normalizedSource), normalizedQuery);
        }

        List<Document> dbResults = documentRepository.searchDocuments(normalizedQuery, normalizedType,
                normalizedSource);

        if (hasQuery) {
            return rankAndSort(combineWithYouTube(dbResults, normalizedQuery), normalizedQuery);
        }

        return rankAndSort(dbResults, normalizedQuery);
    }

    private List<Document> combineWithYouTube(List<Document> dbResults, String query) {
        List<Document> combinedResults = new ArrayList<>(dbResults);
        combinedResults.addAll(youTubeApiService.searchYouTube(query));
        return combinedResults;
    }

    private List<Document> rankAndSort(List<Document> results, String query) {
        for (Document document : results) {
            double score = rankingService.calculateScore(document, query);
            document.setScore(score);
        }

        results.sort(Comparator.comparing(Document::getScore, Comparator.nullsLast(Double::compareTo)).reversed());
        return results;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
