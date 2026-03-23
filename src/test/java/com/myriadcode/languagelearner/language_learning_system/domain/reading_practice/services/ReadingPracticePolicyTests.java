package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services;

import com.myriadcode.fsrs.api.enums.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingPracticePolicyTests {

    private final ReadingPracticePolicy policy = new ReadingPracticePolicy();

    @Test
    @DisplayName("Reading selection prioritizes due cards inside the review ratio budget")
    void prioritizesDueCardsBeforeUpcomingCards() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("due-weak", State.REVIEW, "2026-01-01T00:00:00Z", rotationHour.minusSeconds(7200), 0.52, 1, rotationHour.minusSeconds(86400)),
                candidate("due-strong", State.REVIEW, "2026-01-01T00:01:00Z", rotationHour.minusSeconds(3600), 0.96, 0, rotationHour.minusSeconds(3600)),
                candidate("upcoming", State.LEARNING, "2026-01-01T00:02:00Z", rotationHour.plusSeconds(600), 0.84, 1, rotationHour.minusSeconds(7200))
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).extracting(ReadingPracticeCandidate::flashCardId)
                .containsExactly("due-weak", "upcoming");
    }

    @Test
    @DisplayName("Reading selection picks nearest due upcoming cards inside ratio budget")
    void fallsBackToNearestDueUpcomingCards() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("later", State.REVIEW, "2026-01-01T00:00:00Z", rotationHour.plusSeconds(7200), 0.93, 0, rotationHour.minusSeconds(7200)),
                candidate("soon", State.REVIEW, "2026-01-01T00:01:00Z", rotationHour.plusSeconds(600), 0.88, 0, rotationHour.minusSeconds(3600)),
                candidate("middle", State.LEARNING, "2026-01-01T00:02:00Z", rotationHour.plusSeconds(1800), 0.86, 1, rotationHour.minusSeconds(10800))
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).extracting(ReadingPracticeCandidate::flashCardId)
                .startsWith("soon")
                .containsExactlyInAnyOrder("soon", "middle");
    }

    @Test
    @DisplayName("Reading selection applies dynamic ratio targets when all states are available")
    void appliesDynamicRatioTargetsWhenAllStatesAreAvailable() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new java.util.ArrayList<ReadingPracticeCandidate>();

        for (int i = 1; i <= 12; i++) {
            candidates.add(candidate(
                    "review-" + i,
                    State.REVIEW,
                    "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3600L * i),
                    0.95 - (i * 0.01),
                    0,
                    rotationHour.minusSeconds(7200L * i)
            ));
        }
        for (int i = 1; i <= 9; i++) {
            candidates.add(candidate(
                    "re-learning-" + i,
                    State.RE_LEARNING,
                    "2026-01-01T02:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3000L * i),
                    0.95,
                    1,
                    rotationHour.minusSeconds(5400L * i)
            ));
        }
        for (int i = 1; i <= 6; i++) {
            candidates.add(candidate(
                    "learning-" + i,
                    State.LEARNING,
                    "2026-01-01T03:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(2400L * i),
                    0.95,
                    1,
                    rotationHour.minusSeconds(4800L * i)
            ));
        }
        for (int i = 1; i <= 3; i++) {
            candidates.add(candidate(
                    "new-" + i,
                    State.NEW,
                    "2026-01-01T04:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(1800L * i),
                    Double.NaN,
                    0,
                    null
            ));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(30);
        assertThat(selected.stream().filter(candidate -> candidate.state() == State.REVIEW)).hasSize(12);
        assertThat(selected.stream().filter(candidate -> candidate.state() == State.RE_LEARNING)).hasSize(9);
        assertThat(selected.stream().filter(candidate -> candidate.state() == State.LEARNING)).hasSize(6);
        assertThat(selected.stream().filter(candidate -> candidate.state() == State.NEW)).hasSize(3);
    }

    @Test
    @DisplayName("Reading selection does not force-fill missing ratio buckets")
    void doesNotForceFillMissingRatioBuckets() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new java.util.ArrayList<ReadingPracticeCandidate>();

        for (int i = 1; i <= 30; i++) {
            candidates.add(candidate(
                    "review-stable-" + i,
                    State.REVIEW,
                    "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(900L * i),
                    0.97,
                    0,
                    rotationHour.minusSeconds(1200L * i)
            ));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(12);
        assertThat(selected).allMatch(candidate -> candidate.state() == State.REVIEW);
    }

    private ReadingPracticeCandidate candidate(String cardId,
                                               State state,
                                               String createdAt,
                                               Instant due,
                                               double retrievability,
                                               int lapses,
                                               Instant lastReview) {
        return new ReadingPracticeCandidate(
                cardId,
                "vocab-" + cardId,
                state,
                Instant.parse(createdAt),
                due,
                retrievability,
                lapses,
                lastReview
        );
    }
}
