package com.dora.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dora.backend.entity.Document;
import java.util.List;
import org.junit.jupiter.api.Test;

class RankingServiceTest {

    private final RankingService rankingService = new RankingService();

    @Test
    void calculateScoreShouldWeightTitleHigherThanDescription() {
        Document document = new Document();
        document.setTitle("Data Structures Handbook");
        document.setDescription("A practical guide to advanced data topics");

        double score = rankingService.calculateScore(document, List.of("data", "structures"));

        assertThat(score).isEqualTo(5.0);
    }

    @Test
    void calculateScoreShouldReturnZeroWhenThereAreNoKeywords() {
        Document document = new Document();
        document.setTitle("Algorithms");
        document.setDescription("Introduction to algorithms");

        double score = rankingService.calculateScore(document, List.of());

        assertThat(score).isZero();
    }
}
