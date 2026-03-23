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
    @DisplayName("Writing selection applies 0.65/0.20/0.15 ratio and excludes new cards")
    void appliesRatioAndExcludesNewCards() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new ArrayList<WritingPracticeCandidate>();

        for (int i = 1; i <= 13; i++) {
            candidates.add(candidate("r" + i, State.REVIEW, "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3600L * i), 0.98, 0, rotationHour.minusSeconds(1800L * i)));
        }
        for (int i = 1; i <= 4; i++) {
            candidates.add(candidate("l" + i, State.LEARNING, "2026-01-01T01:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(1200L * i), 0.95, 0, rotationHour.minusSeconds(1200L * i)));
        }
        for (int i = 1; i <= 3; i++) {
            candidates.add(candidate("rl" + i, State.RE_LEARNING, "2026-01-01T02:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(900L * i), 0.95, 0, rotationHour.minusSeconds(900L * i)));
        }
        for (int i = 1; i <= 4; i++) {
            candidates.add(candidate("n" + i, State.NEW, "2026-01-01T03:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(600L * i), Double.NaN, 0, null));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(c -> c.state() == State.NEW)).isEmpty();
        assertThat(selected.stream().filter(c -> c.state() == State.REVIEW)).hasSize(13);
        assertThat(selected.stream().filter(c -> c.state() == State.LEARNING)).hasSize(4);
        assertThat(selected.stream().filter(c -> c.state() == State.RE_LEARNING)).hasSize(3);
    }

    @Test
    @DisplayName("Writing selection still prioritizes the strongest review card in tiny sets")
    void prefersHigherRetrievabilityCardsInsideBucket() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("stable", State.REVIEW, "2026-01-01T00:00:00Z",
                        rotationHour.minusSeconds(600), 0.97, 0, rotationHour.minusSeconds(600)),
                candidate("fragile", State.REVIEW, "2026-01-01T00:01:00Z",
                        rotationHour.minusSeconds(600), 0.61, 0, rotationHour.minusSeconds(600))
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).extracting(WritingPracticeCandidate::flashCardId)
                .containsExactly("stable");
    }

    @Test
    @DisplayName("Writing selection does not force-fill missing ratio buckets")
    void doesNotForceFillWhenRatioBucketsAreMissing() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new ArrayList<WritingPracticeCandidate>();

        for (int i = 1; i <= 30; i++) {
            candidates.add(candidate("stable-" + i, State.REVIEW, "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3600L * i), 0.98, 0, rotationHour.minusSeconds(1800L * i)));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected).allMatch(candidate -> candidate.state() == State.REVIEW);
    }

    @Test
    @DisplayName("Writing selection keeps fragile cards under cap")
    void keepsFragileCardsUnderCap() {
        var rotationHour = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new ArrayList<WritingPracticeCandidate>();

        for (int i = 1; i <= 20; i++) {
            candidates.add(candidate("stable-" + i, State.REVIEW, "2026-01-01T00:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(3600L * i), 0.98, 0, rotationHour.minusSeconds(1800L * i)));
        }
        for (int i = 1; i <= 10; i++) {
            candidates.add(candidate("fragile-" + i, State.REVIEW, "2026-01-01T01:%02d:00Z".formatted(i % 60),
                    rotationHour.minusSeconds(300L * i), 0.45, 2, rotationHour.minusSeconds(300L * i)));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(candidate -> candidate.retrievability() <= 0.50))
                .hasSizeLessThanOrEqualTo(2);
    }

    private WritingPracticeCandidate candidate(String cardId,
                                               State state,
                                               String createdAt,
                                               Instant due,
                                               double retrievability,
                                               int lapses,
                                               Instant lastReview) {
        return new WritingPracticeCandidate(
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
