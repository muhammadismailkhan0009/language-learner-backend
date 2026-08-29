package com.myriadcode.languagelearner.behavior.reading_vocabulary_extraction;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingScenarioRatingProgressTests {

    @Test
    void completesOnlyWhenAcceptedRatingsReachAttachedCardCount() {
        var scenario = scenario(0, false, 2);

        var first = scenario.recordAcceptedRating();
        var second = first.scenario().recordAcceptedRating();
        var repeated = second.scenario().recordAcceptedRating();

        assertThat(first.completedNow()).isFalse();
        assertThat(first.scenario().ratedCardsCount()).isEqualTo(1);
        assertThat(second.completedNow()).isTrue();
        assertThat(second.scenario().ratedCardsCount()).isEqualTo(2);
        assertThat(second.scenario().allCardsRated()).isTrue();
        assertThat(repeated.completedNow()).isFalse();
        assertThat(repeated.scenario()).isEqualTo(second.scenario());
    }

    @Test
    void scenarioWithoutCardsNeverCompletes() {
        var result = scenario(0, false, 0).recordAcceptedRating();

        assertThat(result.completedNow()).isFalse();
        assertThat(result.scenario().ratedCardsCount()).isZero();
        assertThat(result.scenario().allCardsRated()).isFalse();
    }

    private ReadingPracticeScenario scenario(int ratedCardsCount, boolean allCardsRated, int cardCount) {
        var usages = java.util.stream.IntStream.range(0, cardCount)
                .mapToObj(index -> new ReadingVocabularyUsage(
                        new ReadingVocabularyUsage.ReadingVocabularyUsageId("usage-" + index),
                        "card-" + index,
                        "vocab-" + index))
                .toList();
        return new ReadingPracticeScenario(
                new ReadingPracticeScenario.ReadingPracticeScenarioId("scenario-1"),
                "Topic", "Text", 0, List.of(), usages, ratedCardsCount, allCardsRated);
    }
}
