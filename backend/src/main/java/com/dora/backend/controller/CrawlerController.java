package com.dora.backend.controller;

import com.dora.backend.dto.CrawlerRequest;
import com.dora.backend.service.CrawlerService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    private final CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping("/start")
    public Map<String, String> startCrawling(@RequestBody CrawlerRequest request) {
        crawlerService.crawlPage(request.getUrl(), request.getMaxDepth(), request.getMaxPages());
        return Map.of("message", "Page crawled successfully");
    }

    @PostMapping("/academic")
    public Map<String, Object> crawlAcademicSources(@RequestBody(required = false) CrawlerRequest request) {
        Integer maxDepth = request == null ? null : request.getMaxDepth();
        Integer maxPages = request == null ? null : request.getMaxPages();
        int crawledSeeds = crawlerService.crawlAcademicSources(maxDepth, maxPages);

        return Map.of(
                "message", "Academic seed crawling completed",
                "seedCount", crawlerService.getAcademicSeedUrls().size(),
                "successfulSeeds", crawledSeeds);
    }
}
