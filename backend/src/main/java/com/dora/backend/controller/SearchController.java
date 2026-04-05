package com.dora.backend.controller;

import com.dora.backend.dto.SearchResponse;
import com.dora.backend.entity.Document;
import com.dora.backend.service.SearchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public SearchResponse search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "years", required = false) String years,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid query parameter");
        }

        return searchService.search(query, source, type, years, page, size);
    }
}
