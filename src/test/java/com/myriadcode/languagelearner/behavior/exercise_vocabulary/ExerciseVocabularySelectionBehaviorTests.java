package com.myriadcode.languagelearner.behavior.exercise_vocabulary;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingPracticePolicy;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseVocabularySelectionBehaviorTests {

    private static final Instant NOW = Instant.parse("2026-08-19T12:00:00Z");

    @Test
    @DisplayName("Earlier due date outranks retrievability in reading and writing")
    void earlierDueDateOutranksRetrievability() {
        var reading = new ReadingPracticePolicy().selectCandidates(List.of(
                readingCandidate("easy-later", State.REVIEW, NOW.plusSeconds(86400), 0.99, 0, NOW.minusSeconds(100)),
                readingCandidate("urgent", State.REVIEW, NOW.minusSeconds(60), 0.72, 0, NOW.minusSeconds(200))
        ), NOW, Map.of());
        var writing = new WritingPracticePolicy().selectCandidates(List.of(
                writingCandidate("easy-later", State.REVIEW, NOW.plusSeconds(86400), 0.99, 0, NOW.minusSeconds(100)),
                writingCandidate("urgent", State.REVIEW, NOW.minusSeconds(60), 0.72, 0, NOW.minusSeconds(200))
        ), NOW, Map.of());

        assertThat(reading).extracting(ReadingPracticeCandidate::flashCardId)
                .containsExactly("urgent", "easy-later");
        assertThat(writing).extracting(WritingPracticeCandidate::flashCardId)
                .containsExactly("urgent", "easy-later");
    }

    @Test
    @DisplayName("Recent exercise usage is a late tie-breaker")
    void recentUsageBreaksOtherwiseEqualPriority() {
        var reading = new ReadingPracticePolicy().selectCandidates(List.of(
                readingCandidate("frequent", State.REVIEW, NOW, 0.80, 1, NOW.minusSeconds(300)),
                readingCandidate("rare", State.REVIEW, NOW, 0.80, 1, NOW.minusSeconds(300))
        ), NOW, Map.of("v-frequent", 7, "v-rare", 1));
        var writing = new WritingPracticePolicy().selectCandidates(List.of(
                writingCandidate("frequent", State.REVIEW, NOW, 0.80, 1, NOW.minusSeconds(300)),
                writingCandidate("rare", State.REVIEW, NOW, 0.80, 1, NOW.minusSeconds(300))
        ), NOW, Map.of("v-frequent", 7, "v-rare", 1));

        assertThat(reading.getFirst().flashCardId()).isEqualTo("rare");
        assertThat(writing.getFirst().flashCardId()).isEqualTo("rare");
    }

    @Test
    @DisplayName("Reading NEW cards use creation time before recent usage")
    void readingNewCardsUseCreationBeforeRecentUsage() {
        var older = new ReadingPracticeCandidate("older", "v-older", State.NEW,
                NOW.minusSeconds(600), null, Double.NaN, 0, null);
        var newer = new ReadingPracticeCandidate("newer", "v-newer", State.NEW,
                NOW.minusSeconds(300), null, Double.NaN, 0, null);

        var selected = new ReadingPracticePolicy().selectCandidates(
                List.of(newer, older), NOW, Map.of("v-older", 9, "v-newer", 0));

        assertThat(selected).extracting(ReadingPracticeCandidate::flashCardId)
                .containsExactly("older", "newer");
    }

    @Test
    @DisplayName("Reading represents all four non-empty states and writing represents three")
    void representsEveryEligibleState() {
        var readingCandidates = List.of(
                readingCandidate("review", State.REVIEW, NOW, 0.80, 0, NOW),
                readingCandidate("learning", State.LEARNING, NOW, 0.80, 0, NOW),
                readingCandidate("relearning", State.RE_LEARNING, NOW, 0.80, 0, NOW),
                readingCandidate("new", State.NEW, null, Double.NaN, 0, null)
        );
        var writingCandidates = List.of(
                writingCandidate("review", State.REVIEW, NOW, 0.80, 0, NOW),
                writingCandidate("learning", State.LEARNING, NOW, 0.80, 0, NOW),
                writingCandidate("relearning", State.RE_LEARNING, NOW, 0.80, 0, NOW),
                writingCandidate("new", State.NEW, null, Double.NaN, 0, null)
        );

        assertThat(new ReadingPracticePolicy().selectCandidates(readingCandidates, NOW, Map.of()))
                .extracting(ReadingPracticeCandidate::state)
                .containsExactlyInAnyOrder(State.REVIEW, State.LEARNING, State.RE_LEARNING, State.NEW);
        assertThat(new WritingPracticePolicy().selectCandidates(writingCandidates, NOW, Map.of()))
                .extracting(WritingPracticeCandidate::state)
                .containsExactlyInAnyOrder(State.REVIEW, State.LEARNING, State.RE_LEARNING);
    }

    @Test
    @DisplayName("Reading weak threshold caps unstable opportunity pool at ten")
    void readingCapsVeryWeakCardsAtTen() {
        var candidates = new ArrayList<ReadingPracticeCandidate>();
        for (int index = 0; index < 12; index++) {
            candidates.add(readingCandidate("weak-" + index, State.REVIEW,
                    NOW.minusSeconds(index + 1), 0.60, 0, NOW.minusSeconds(1000)));
        }
        candidates.add(readingCandidate("stable", State.REVIEW, NOW.plusSeconds(1), 0.61, 0, NOW));

        var selected = new ReadingPracticePolicy().selectCandidates(candidates, NOW, Map.of());

        assertThat(selected.stream().filter(candidate -> candidate.retrievability() <= 0.60)).hasSize(10);
        assertThat(selected).extracting(ReadingPracticeCandidate::flashCardId).contains("stable");
    }

    @Test
    @DisplayName("Selectors never exceed one hundred candidates")
    void selectorsCapOpportunityPoolAtOneHundred() {
        var readingCandidates = new ArrayList<ReadingPracticeCandidate>();
        var writingCandidates = new ArrayList<WritingPracticeCandidate>();
        for (int index = 0; index < 120; index++) {
            readingCandidates.add(readingCandidate("r-" + index, State.REVIEW,
                    NOW.plusSeconds(index), 0.90, 0, NOW));
            writingCandidates.add(writingCandidate("w-" + index, State.REVIEW,
                    NOW.plusSeconds(index), 0.90, 0, NOW));
        }

        assertThat(new ReadingPracticePolicy().selectCandidates(readingCandidates, NOW, Map.of())).hasSize(100);
        assertThat(new WritingPracticePolicy().selectCandidates(writingCandidates, NOW, Map.of())).hasSize(100);
    }

    private ReadingPracticeCandidate readingCandidate(String id, State state, Instant due,
                                                       double retrievability, int lapses, Instant lastReview) {
        return new ReadingPracticeCandidate(id, "v-" + id, state, NOW.minusSeconds(3600), due,
                retrievability, lapses, lastReview);
    }

    private WritingPracticeCandidate writingCandidate(String id, State state, Instant due,
                                                       double retrievability, int lapses, Instant lastReview) {
        return new WritingPracticeCandidate(id, "v-" + id, state, NOW.minusSeconds(3600), due,
                retrievability, lapses, lastReview);
    }
}
