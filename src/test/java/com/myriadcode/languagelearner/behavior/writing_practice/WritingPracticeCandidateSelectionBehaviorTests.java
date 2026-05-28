package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WritingPracticeCandidateSelectionBehaviorTests {

    private final WritingPracticeCandidateAssembler assembler = new WritingPracticeCandidateAssembler();

    @Test
    void filterByPracticeVocabularyKeepsOnlyPracticeIds() {
        var filtered = assembler.filterByPracticeVocabulary(
                List.of(
                        new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true),
                        new VocabularyFlashcardReviewRecord("f-2", "v-2", State.REVIEW, true)
                ),
                Set.of("v-2")
        );

        assertThat(filtered).extracting(VocabularyFlashcardReviewRecord::vocabularyId)
                .containsExactly("v-2");
    }

    @Test
    void buildCandidatesKeepsOnlyReversedWithKnownVocabularyRecord() {
        var reviews = List.of(
                new VocabularyFlashcardReviewRecord("f-r", "v-r", State.REVIEW, true),
                new VocabularyFlashcardReviewRecord("f-front", "v-r", State.REVIEW, false),
                new VocabularyFlashcardReviewRecord("f-missing", "v-x", State.REVIEW, true)
        );

        var vocab = Map.of(
                "v-r",
                new PrivateVocabularyRecord(
                        "v-r",
                        "u-1",
                        "gehen",
                        "to go",
                        "WORD",
                        null,
                        List.of(),
                        null,
                        Instant.parse("2026-01-01T00:00:00Z")
                )
        );

        var candidates = assembler.buildCandidates(reviews, vocab);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.getFirst().flashCardId()).isEqualTo("f-r");
        assertThat(candidates.getFirst().vocabularyId()).isEqualTo("v-r");
    }

    @Test
    void buildUsagesDeduplicatesByFlashcardAndMatchesNormalizedSurface() {
        var selected = List.of(
                new com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticeCandidate(
                        "f-1",
                        "v-1",
                        State.REVIEW,
                        Instant.EPOCH,
                        null,
                        Double.NaN,
                        0,
                        null
                ),
                new com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticeCandidate(
                        "f-1",
                        "v-1",
                        State.REVIEW,
                        Instant.EPOCH,
                        null,
                        Double.NaN,
                        0,
                        null
                )
        );

        var records = Map.of(
                "v-1",
                new PrivateVocabularyRecord(
                        "v-1",
                        "u-1",
                        "Haus",
                        "house",
                        "WORD",
                        null,
                        List.of(),
                        null,
                        Instant.now()
                )
        );

        var usages = assembler.buildUsages(selected, records, Set.of("haus"));

        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().flashCardId()).isEqualTo("f-1");
        assertThat(usages.getFirst().vocabularyId()).isEqualTo("v-1");
    }
}
