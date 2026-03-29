package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final DocumentRepository documentRepository;
    private final YouTubeApiService youTubeApiService;

    public SearchService(DocumentRepository documentRepository, YouTubeApiService youTubeApiService) {
        this.documentRepository = documentRepository;
        this.youTubeApiService = youTubeApiService;
    }

    public List<Document> search(String query, String type, String source) {
        String normalizedQuery = normalize(query);
        String normalizedType = normalize(type);
        String normalizedSource = normalize(source);

        boolean hasQuery = isPresent(normalizedQuery);
        boolean hasType = isPresent(normalizedType);
        boolean hasSource = isPresent(normalizedSource);

        if (!hasQuery && !hasType && !hasSource) {
            return documentRepository.findAll();
        }

        if (hasQuery && !hasType && !hasSource) {
            List<Document> dbResults = documentRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedQuery, normalizedQuery);
            return combineWithYouTube(dbResults, normalizedQuery);
        }

        if (!hasQuery && hasType && !hasSource) {
            return documentRepository.findByTypeIgnoreCase(normalizedType);
        }

        if (!hasQuery && !hasType && hasSource) {
            return documentRepository.findBySourceIgnoreCase(normalizedSource);
        }

        List<Document> dbResults = documentRepository.searchDocuments(normalizedQuery, normalizedType,
                normalizedSource);

        if (hasQuery) {
            return combineWithYouTube(dbResults, normalizedQuery);
        }

        return dbResults;
    }

    private List<Document> combineWithYouTube(List<Document> dbResults, String query) {
        List<Document> combinedResults = new ArrayList<>(dbResults);
        combinedResults.addAll(youTubeApiService.searchYouTube(query));
        return combinedResults;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
