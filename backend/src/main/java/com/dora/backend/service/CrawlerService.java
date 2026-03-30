package com.dora.backend.service;

import com.dora.backend.entity.CrawledPage;
import com.dora.backend.entity.Document;
import com.dora.backend.repository.CrawledPageRepository;
import com.dora.backend.repository.DocumentRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class CrawlerService {

    private final CrawledPageRepository crawledPageRepository;
    private final DocumentRepository documentRepository;

    public CrawlerService(CrawledPageRepository crawledPageRepository, DocumentRepository documentRepository) {
        this.crawledPageRepository = crawledPageRepository;
        this.documentRepository = documentRepository;
    }

    public void crawlPage(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }

        String normalizedUrl = url.trim();

        try {
            org.jsoup.nodes.Document parsedDocument = Jsoup.connect(normalizedUrl).get();
            String title = parsedDocument.title();
            String content = parsedDocument.body() != null ? parsedDocument.body().text() : "";

            CrawledPage crawledPage = new CrawledPage();
            crawledPage.setUrl(normalizedUrl);
            crawledPage.setTitle(title);
            crawledPage.setContent(content);
            crawledPage.setSource("crawler");
            crawledPage.setCrawledAt(LocalDateTime.now());
            CrawledPage savedPage = crawledPageRepository.save(crawledPage);

            if (documentRepository.findByUrl(savedPage.getUrl()).isEmpty()) {
                Document document = convertToDocument(savedPage);
                documentRepository.save(document);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to crawl page: " + url, ex);
        }
    }

    private Document convertToDocument(CrawledPage page) {
        Document document = new Document();
        document.setTitle(page.getTitle());
        document.setUrl(page.getUrl());
        document.setDescription(buildDescription(page.getContent()));
        document.setSource("crawler");
        document.setType("article");
        document.setYear(Year.now().getValue());
        document.setScore(0.0);
        return document;
    }

    private String buildDescription(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        int maxLength = 300;
        return content.length() <= maxLength ? content : content.substring(0, maxLength);
    }
}
