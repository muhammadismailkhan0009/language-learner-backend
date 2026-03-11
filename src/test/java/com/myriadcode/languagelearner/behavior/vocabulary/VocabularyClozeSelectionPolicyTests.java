package com.myriadcode.languagelearner.behavior.vocabulary;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyClozeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyClozeSelectionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyClozeSelectionPolicyTests {

    private final VocabularyClozeSelectionPolicy policy = new VocabularyClozeSelectionPolicy();

    @Test
    @DisplayName("Cloze selection prioritizes due cards before upcoming cards")
    void prioritizesDueCardsBeforeUpcomingCards() {
        var now = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("due-weak", State.REVIEW, now.minusSeconds(7200), 2.0, 6.0, 1, now.minusSeconds(86400)),
                candidate("due-strong", State.REVIEW, now.minusSeconds(3600), 8.0, 4.0, 0, now.minusSeconds(3600)),
                candidate("upcoming-1", State.REVIEW, now.plusSeconds(900), 1.5, 5.0, 0, now.minusSeconds(7200)),
                candidate("upcoming-2", State.LEARNING, now.plusSeconds(600), 1.0, 7.0, 1, now.minusSeconds(10800))
        );

        var selected = policy.selectCandidates("user-1", candidates, now);

        assertThat(selected).extracting(VocabularyClozeCandidate::flashcardId)
                .startsWith("due-weak", "due-strong");
    }

    @Test
    @DisplayName("Cloze selection falls back to nearest due upcoming cards when nothing is due")
    void selectsNearestDueUpcomingCardsWhenNothingIsDue() {
        var now = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = List.of(
                candidate("later", State.REVIEW, now.plusSeconds(7200), 2.0, 5.0, 0, now.minusSeconds(7200)),
                candidate("soon", State.REVIEW, now.plusSeconds(600), 4.0, 4.0, 0, now.minusSeconds(3600)),
                candidate("mid", State.LEARNING, now.plusSeconds(1800), 1.5, 6.0, 1, now.minusSeconds(10800))
        );

        var selected = policy.selectCandidates("user-1", candidates, now);

        assertThat(selected).extracting(VocabularyClozeCandidate::flashcardId)
                .containsExactly("soon", "mid", "later");
    }

    @Test
    @DisplayName("Cloze selection caps new cards even when many new cards rank highly")
    void capsNewCards() {
        var now = Instant.parse("2026-03-11T10:00:00Z");
        var candidates = new ArrayList<VocabularyClozeCandidate>();

        for (int i = 1; i <= 20; i++) {
            candidates.add(candidate(
                    "review-" + i,
                    State.REVIEW,
                    now.minusSeconds(3600L * i),
                    2.0 + i,
                    5.0,
                    0,
                    now.minusSeconds(7200L * i)
            ));
        }
        for (int i = 1; i <= 6; i++) {
            candidates.add(candidate(
                    "new-" + i,
                    State.NEW,
                    now.minusSeconds(1800L * i),
                    1.0,
                    8.0,
                    0,
                    null
            ));
        }

        var selected = policy.selectCandidates("user-1", candidates, now);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(candidate -> candidate.state() == State.NEW)).hasSizeLessThanOrEqualTo(3);
    }

    private VocabularyClozeCandidate candidate(String flashcardId,
                                               State state,
                                               Instant due,
                                               double stability,
                                               double difficulty,
                                               int lapses,
                                               Instant lastReview) {
        return new VocabularyClozeCandidate(
                flashcardId,
                "vocab-" + flashcardId,
                state,
                Instant.parse("2026-01-01T00:00:00Z"),
                due,
                stability,
                difficulty,
                lapses,
                lastReview
        );
    }
}
