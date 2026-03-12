package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services;

import com.myriadcode.fsrs.api.enums.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WritingPracticePolicyTests {

    private final WritingPracticePolicy policy = new WritingPracticePolicy();

    @Test
    @DisplayName("Writing selection excludes new cards and heavily favors review cards")
    void excludesNewCardsAndFavorsReview() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new ArrayList<WritingPracticeCandidate>();

        for (int i = 1; i <= 15; i++) {
            candidates.add(candidate("r" + i, State.REVIEW, "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3600L * i), 8.0, 3.0, 0, rotationHour.minusSeconds(1800L * i)));
        }
        for (int i = 1; i <= 3; i++) {
            candidates.add(candidate("l" + i, State.LEARNING, "2026-01-01T01:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(1200L * i), 5.0, 4.0, 0, rotationHour.minusSeconds(1200L * i)));
        }
        for (int i = 1; i <= 2; i++) {
            candidates.add(candidate("rl" + i, State.RE_LEARNING, "2026-01-01T02:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(900L * i), 4.0, 5.0, 1, rotationHour.minusSeconds(900L * i)));
        }
        for (int i = 1; i <= 4; i++) {
            candidates.add(candidate("n" + i, State.NEW, "2026-01-01T03:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(600L * i), 2.0, 8.0, 0, null));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(c -> c.state() == State.NEW)).isEmpty();
        assertThat(selected.stream().filter(c -> c.state() == State.REVIEW)).hasSize(15);
        assertThat(selected.stream().filter(c -> c.state() == State.LEARNING)).hasSize(3);
        assertThat(selected.stream().filter(c -> c.state() == State.RE_LEARNING)).hasSize(2);
    }

    @Test
    @DisplayName("Writing selection prefers more stable cards inside the same bucket")
    void prefersMoreStableCardsInsideBucket() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("stable", State.REVIEW, "2026-01-01T00:00:00Z",
                        rotationHour.minusSeconds(600), 8.0, 3.0, 0, rotationHour.minusSeconds(600)),
                candidate("fragile", State.REVIEW, "2026-01-01T00:01:00Z",
                        rotationHour.minusSeconds(600), 2.0, 3.0, 0, rotationHour.minusSeconds(600)),
                candidate("learning", State.LEARNING, "2026-01-01T00:02:00Z",
                        rotationHour.minusSeconds(300), 5.0, 4.0, 0, rotationHour.minusSeconds(300))
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).extracting(WritingPracticeCandidate::flashCardId)
                .startsWith("stable", "fragile", "learning");
    }

    @Test
    @DisplayName("Writing selection limits fragile cards")
    void limitsFragileCards() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new ArrayList<WritingPracticeCandidate>();

        for (int i = 1; i <= 20; i++) {
            candidates.add(candidate("stable-" + i, State.REVIEW, "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3600L * i), 7.0, 3.0, 0, rotationHour.minusSeconds(1800L * i)));
        }
        for (int i = 1; i <= 10; i++) {
            candidates.add(candidate("fragile-" + i, State.REVIEW, "2026-01-01T01:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(300L * i), 2.0, 5.0, 2, rotationHour.minusSeconds(300L * i)));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(candidate -> candidate.lapses() >= 2 || candidate.stability() <= 2.5))
                .hasSizeLessThanOrEqualTo(2);
    }

    private WritingPracticeCandidate candidate(String cardId,
                                               State state,
                                               String createdAt,
                                               Instant due,
                                               double stability,
                                               double difficulty,
                                               int lapses,
                                               Instant lastReview) {
        return new WritingPracticeCandidate(
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
