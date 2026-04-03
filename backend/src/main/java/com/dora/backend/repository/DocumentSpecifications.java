package com.dora.backend.repository;

import com.dora.backend.entity.Document;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class DocumentSpecifications {

    private DocumentSpecifications() {
    }

    public static Specification<Document> hasAnyKeywordInTitleOrDescription(List<String> keywords) {
        return (root, query, criteriaBuilder) -> {
            if (keywords == null || keywords.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = keywords.stream()
                    .map(keyword -> "%" + keyword.toLowerCase(Locale.ROOT) + "%")
                    .map(pattern -> criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)))
                    .toList();

            return criteriaBuilder.or(predicates.toArray(Predicate[]::new));
        };
    }
}
