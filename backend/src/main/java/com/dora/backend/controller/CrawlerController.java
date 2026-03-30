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
        crawlerService.crawlPage(request.getUrl());
        return Map.of("message", "Page crawled successfully");
    }
}
