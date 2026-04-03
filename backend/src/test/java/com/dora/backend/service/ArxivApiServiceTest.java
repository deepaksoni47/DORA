package com.dora.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ArxivApiServiceTest {

    private final ArxivApiService arxivApiService = new ArxivApiService();

    @Test
    void buildSearchQueryShouldUseAllPrefixForSingleTerm() {
        assertThat(arxivApiService.buildSearchQuery("electron")).isEqualTo("all:electron");
    }

    @Test
    void buildSearchQueryShouldBuildBooleanAndQueryForMultipleTerms() {
        assertThat(arxivApiService.buildSearchQuery("data structures tutorial"))
                .isEqualTo("all:data AND all:structures AND all:tutorial");
    }

    @Test
    void buildSearchQueryShouldLimitVeryLongQueries() {
        assertThat(arxivApiService.buildSearchQuery("graph neural networks message passing tutorial"))
                .isEqualTo("all:graph AND all:neural AND all:networks AND all:message");
    }
}
