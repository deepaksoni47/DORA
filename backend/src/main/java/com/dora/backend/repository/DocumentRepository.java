package com.dora.backend.repository;

import com.dora.backend.entity.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    List<Document> findByTypeIgnoreCase(String type);

    List<Document> findBySourceIgnoreCase(String source);

    @Query("""
            SELECT d
            FROM Document d
            WHERE (:query IS NULL OR :query = ''
            OR LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:type IS NULL OR :type = '' OR LOWER(d.type) = LOWER(:type))
              AND (:source IS NULL OR :source = '' OR LOWER(d.source) = LOWER(:source))
            """)
    List<Document> searchDocuments(
            @Param("query") String query,
            @Param("type") String type,
            @Param("source") String source);
}
