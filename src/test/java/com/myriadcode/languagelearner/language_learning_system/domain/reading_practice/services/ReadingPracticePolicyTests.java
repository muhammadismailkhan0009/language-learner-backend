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
    @DisplayName("Reading selection prioritizes due cards before upcoming cards")
    void prioritizesDueCardsBeforeUpcomingCards() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("due-weak", State.REVIEW, "2026-01-01T00:00:00Z", rotationHour.minusSeconds(7200), 2.0, 5.0, 1, rotationHour.minusSeconds(86400)),
                candidate("due-strong", State.REVIEW, "2026-01-01T00:01:00Z", rotationHour.minusSeconds(3600), 8.0, 4.0, 0, rotationHour.minusSeconds(3600)),
                candidate("upcoming", State.LEARNING, "2026-01-01T00:02:00Z", rotationHour.plusSeconds(600), 1.0, 7.0, 1, rotationHour.minusSeconds(7200))
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).extracting(ReadingPracticeCandidate::flashCardId)
                .containsExactly("due-weak", "due-strong", "upcoming");
    }

    @Test
    @DisplayName("Reading selection falls back to nearest due upcoming cards when nothing is due")
    void fallsBackToNearestDueUpcomingCards() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("later", State.REVIEW, "2026-01-01T00:00:00Z", rotationHour.plusSeconds(7200), 2.0, 5.0, 0, rotationHour.minusSeconds(7200)),
                candidate("soon", State.REVIEW, "2026-01-01T00:01:00Z", rotationHour.plusSeconds(600), 4.0, 4.0, 0, rotationHour.minusSeconds(3600)),
                candidate("middle", State.LEARNING, "2026-01-01T00:02:00Z", rotationHour.plusSeconds(1800), 1.0, 6.0, 1, rotationHour.minusSeconds(10800))
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).extracting(ReadingPracticeCandidate::flashCardId)
                .startsWith("soon")
                .containsExactlyInAnyOrder("soon", "middle", "later");
    }

    @Test
    @DisplayName("Reading selection caps new cards when enough non-new candidates exist")
    void capsNewCards() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new java.util.ArrayList<ReadingPracticeCandidate>();

        for (int i = 1; i <= 20; i++) {
            candidates.add(candidate(
                    "review-" + i,
                    State.REVIEW,
                    "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3600L * i),
                    2.0 + i,
                    5.0,
                    0,
                    rotationHour.minusSeconds(7200L * i)
            ));
        }
        for (int i = 1; i <= 6; i++) {
            candidates.add(candidate(
                    "new-" + i,
                    State.NEW,
                    "2026-01-01T01:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(1800L * i),
                    1.0,
                    8.0,
                    0,
                    null
            ));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(c -> c.state() == State.NEW)).hasSizeLessThanOrEqualTo(2);
    }

    private ReadingPracticeCandidate candidate(String cardId,
                                               State state,
                                               String createdAt,
                                               Instant due,
                                               double stability,
                                               double difficulty,
                                               int lapses,
                                               Instant lastReview) {
        return new ReadingPracticeCandidate(
                cardId,
                "vocab-" + cardId,
                state,
                Instant.parse(createdAt),
                due,
                stability,
                difficulty,
                lapses,
                lastReview
        );
    }
}
