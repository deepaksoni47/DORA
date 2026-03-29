package com.dora.backend.service;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final DocumentRepository documentRepository;

    public SearchService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
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
            return documentRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedQuery, normalizedQuery);
        }

        if (!hasQuery && hasType && !hasSource) {
            return documentRepository.findByTypeIgnoreCase(normalizedType);
        }

        if (!hasQuery && !hasType && hasSource) {
            return documentRepository.findBySourceIgnoreCase(normalizedSource);
        }

        return documentRepository.searchDocuments(normalizedQuery, normalizedType, normalizedSource);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
