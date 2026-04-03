package com.dora.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class QueryPreprocessorServiceTest {

    private final QueryPreprocessorService queryPreprocessorService = new QueryPreprocessorService();

    @Test
    void preprocessShouldLowercaseRemoveStopWordsAndNormalizeSpaces() {
        String cleanedQuery = queryPreprocessorService.preprocess("I want   to learn data structures");

        assertThat(cleanedQuery).isEqualTo("data structures");
    }

    @Test
    void extractKeywordsShouldSplitCleanedQueryIntoKeywordList() {
        List<String> keywords = queryPreprocessorService.extractKeywords("data structures");

        assertThat(keywords).containsExactly("data", "structures");
    }

    @Test
    void preprocessShouldReturnEmptyStringWhenOnlyStopWordsExist() {
        String cleanedQuery = queryPreprocessorService.preprocess("I want to learn about the");

        assertThat(cleanedQuery).isEmpty();
    }
}
