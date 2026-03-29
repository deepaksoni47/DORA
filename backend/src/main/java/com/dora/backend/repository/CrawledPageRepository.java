package com.dora.backend.repository;

import com.dora.backend.entity.CrawledPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawledPageRepository extends JpaRepository<CrawledPage, Long> {
}
