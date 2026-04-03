package com.dora.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dora.backend.entity.Document;
import com.dora.backend.repository.DocumentRepository;
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

    @Test
    void searchShouldPreprocessExtractKeywordsAndReturnRankedResults() {
        SearchService searchService = new SearchService(
                documentRepository,
                youTubeApiService,
                new RankingService(),
                new QueryPreprocessorService());

        Document titleMatch = new Document();
        titleMatch.setTitle("Data Structures in Java");
        titleMatch.setDescription("Learn the core ideas");
        titleMatch.setSource("crawler");

        Document descriptionMatch = new Document();
        descriptionMatch.setTitle("Algorithm Notes");
        descriptionMatch.setDescription("This covers data structures for interviews");
        descriptionMatch.setSource("crawler");

        when(documentRepository.findAll(any(Specification.class))).thenReturn(List.of(descriptionMatch, titleMatch));
        when(youTubeApiService.searchYouTube(eq("data structures"))).thenReturn(List.of());

        List<Document> results = searchService.search("I want to learn data structures", null, 0, 10);

        assertThat(results).containsExactly(titleMatch, descriptionMatch);
        assertThat(results).extracting(Document::getScore).containsExactly(4.0, 2.0);
        verify(youTubeApiService).searchYouTube("data structures");
    }

    @Test
    void searchShouldShortCircuitWhenNoKeywordsRemainAfterPreprocessing() {
        SearchService searchService = new SearchService(
                documentRepository,
                youTubeApiService,
                new RankingService(),
                new QueryPreprocessorService());

        List<Document> results = searchService.search("I want to learn about the", null, 0, 10);

        assertThat(results).isEmpty();
        verify(documentRepository, never()).findAll(any(Specification.class));
        verify(youTubeApiService, never()).searchYouTube(any());
    }
}
