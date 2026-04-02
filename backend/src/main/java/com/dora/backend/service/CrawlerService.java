package com.dora.backend.service;

import com.dora.backend.entity.CrawledPage;
import com.dora.backend.entity.Document;
import com.dora.backend.repository.CrawledPageRepository;
import com.dora.backend.repository.DocumentRepository;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);
    private static final int DEFAULT_MAX_DEPTH = 1;
    private static final int DEFAULT_MAX_PAGES = 50;

    private final CrawledPageRepository crawledPageRepository;
    private final DocumentRepository documentRepository;

    public CrawlerService(CrawledPageRepository crawledPageRepository, DocumentRepository documentRepository) {
        this.crawledPageRepository = crawledPageRepository;
        this.documentRepository = documentRepository;
    }

    public void crawlPage(String url, Integer maxDepth, Integer maxPages) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }

        String normalizedUrl = url.trim();
        int effectiveMaxDepth = normalizeLimit(maxDepth, DEFAULT_MAX_DEPTH);
        int effectiveMaxPages = normalizeLimit(maxPages, DEFAULT_MAX_PAGES);
        String baseDomain = extractDomain(normalizedUrl);
        Set<String> visitedUrls = new HashSet<>();
        AtomicInteger pagesCrawled = new AtomicInteger(0);

        crawl(normalizedUrl, 0, effectiveMaxDepth, effectiveMaxPages, baseDomain, visitedUrls, pagesCrawled);
    }

    private void crawl(
            String url,
            int depth,
            int maxDepth,
            int maxPages,
            String baseDomain,
            Set<String> visitedUrls,
            AtomicInteger pagesCrawled) {
        if (depth > maxDepth) {
            return;
        }

        if (pagesCrawled.get() >= maxPages) {
            return;
        }

        if (!isHttpUrl(url) || visitedUrls.contains(url)) {
            return;
        }

        if (baseDomain != null && !baseDomain.equalsIgnoreCase(extractDomain(url))) {
            return;
        }

        visitedUrls.add(url);
        logger.info("Crawling URL: {} | depth: {} | pages crawled: {}", url, depth, pagesCrawled.get());

        try {
            org.jsoup.nodes.Document parsedDocument = Jsoup.connect(url).get();
            String title = parsedDocument.title();
            String content = parsedDocument.body() != null ? parsedDocument.body().text() : "";

            CrawledPage crawledPage = new CrawledPage();
            crawledPage.setUrl(url);
            crawledPage.setTitle(title);
            crawledPage.setContent(content);
            crawledPage.setSource("crawler");
            crawledPage.setCrawledAt(LocalDateTime.now());
            CrawledPage savedPage = crawledPageRepository.save(crawledPage);
            pagesCrawled.incrementAndGet();

            if (documentRepository.findByUrl(savedPage.getUrl()).isEmpty()) {
                Document document = convertToDocument(savedPage);
                documentRepository.save(document);
            }

            if (depth == maxDepth || pagesCrawled.get() >= maxPages) {
                return;
            }

            Elements links = parsedDocument.select("a[href]");
            for (Element link : links) {
                if (pagesCrawled.get() >= maxPages) {
                    break;
                }

                String nextUrl = link.attr("abs:href");
                if (nextUrl == null || nextUrl.isBlank()) {
                    continue;
                }

                crawl(nextUrl, depth + 1, maxDepth, maxPages, baseDomain, visitedUrls, pagesCrawled);
            }
        } catch (IOException ex) {
            logger.error("Crawler failed for URL: {}", url, ex);
            throw new IllegalStateException("Failed to crawl page: " + url, ex);
        }
    }

    private int normalizeLimit(Integer value, int defaultValue) {
        if (value == null || value < 0) {
            return defaultValue;
        }
        return value;
    }

    private boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException ex) {
            return null;
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
