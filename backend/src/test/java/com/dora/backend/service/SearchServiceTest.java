package com.dora.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
import com.dora.backend.util.QueryOptimizer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private YouTubeApiService youTubeApiService;

    @Mock
    private GitHubApiService gitHubApiService;

    @Mock
    private ArxivApiService arxivApiService;

    @Mock
    private WikipediaService wikipediaService;

    @Test
    void searchShouldMergeSourcesAndRankStrongerTitleMatchesHigher() {
        SearchService searchService = new SearchService(
                documentRepository,
                youTubeApiService,
                gitHubApiService,
                arxivApiService,
                wikipediaService,
                new RankingService(),
                new QueryOptimizer());

        Document titleMatch = new Document();
        titleMatch.setTitle("Data Structures in Java");
        titleMatch.setDescription("Learn the core ideas");
        titleMatch.setSource("crawler");

        Document descriptionMatch = new Document();
        descriptionMatch.setTitle("Algorithm Notes");
        descriptionMatch.setDescription("This covers data structures for interviews");
        descriptionMatch.setSource("crawler");

        when(documentRepository.findAll(any(Specification.class))).thenReturn(List.of(descriptionMatch, titleMatch));
        when(youTubeApiService.searchYouTube(eq("data structures tutorial"))).thenReturn(List.of());
        when(gitHubApiService.searchRepositories(eq("data structures"))).thenReturn(List.of());
        when(wikipediaService.searchConcept(eq("data structures"))).thenReturn(List.of());
        when(arxivApiService.searchPapers(eq("data structures"))).thenReturn(List.of());

        List<Document> results = searchService.search("I want to learn data structures", null, 0, 10);

        assertThat(results).containsExactly(titleMatch, descriptionMatch);
        assertThat(titleMatch.getScore()).isGreaterThan(descriptionMatch.getScore());
        verify(youTubeApiService, atLeastOnce()).searchYouTube("data structures tutorial");
        verify(gitHubApiService, atLeastOnce()).searchRepositories("data structures");
        verify(wikipediaService, atLeastOnce()).searchConcept("data structures");
        verify(arxivApiService, atLeastOnce()).searchPapers("data structures");
    }

    @Test
    void searchShouldShortCircuitWhenQueryIsNull() {
        SearchService searchService = new SearchService(
                documentRepository,
                youTubeApiService,
                gitHubApiService,
                arxivApiService,
                wikipediaService,
                new RankingService(),
                new QueryOptimizer());

        List<Document> results = searchService.search(null, null, 0, 10);

        assertThat(results).isEmpty();
        verify(documentRepository, never()).findAll(any(Specification.class));
        verify(youTubeApiService, never()).searchYouTube(any());
        verify(gitHubApiService, never()).searchRepositories(any());
        verify(wikipediaService, never()).searchConcept(any());
        verify(arxivApiService, never()).searchPapers(any());
    }
}
