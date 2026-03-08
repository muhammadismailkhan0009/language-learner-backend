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
    @DisplayName("Selects 20 with preserved 6/8/4/2 ratio when enough candidates exist")
    void selectsTwentyWithPreservedRatio() {
        var rotationHour = Instant.parse("2026-01-01T10:00:00Z");
        var candidates = new ArrayList<WritingPracticeCandidate>();

        for (int i = 1; i <= 6; i++) {
            candidates.add(candidate("r" + i, "vr" + i, State.REVIEW, "2026-01-01T00:%02d:00Z".formatted(i % 60)));
        }
        for (int i = 1; i <= 8; i++) {
            candidates.add(candidate("rl" + i, "vrl" + i, State.RE_LEARNING, "2026-01-01T01:%02d:00Z".formatted(i % 60)));
        }
        for (int i = 1; i <= 4; i++) {
            candidates.add(candidate("l" + i, "vl" + i, State.LEARNING, "2026-01-01T02:%02d:00Z".formatted(i % 60)));
        }
        for (int i = 1; i <= 2; i++) {
            candidates.add(candidate("n" + i, "vn" + i, State.NEW, "2026-01-01T03:%02d:00Z".formatted(i % 60)));
        }

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(c -> c.state() == State.REVIEW)).hasSize(6);
        assertThat(selected.stream().filter(c -> c.state() == State.RE_LEARNING)).hasSize(8);
        assertThat(selected.stream().filter(c -> c.state() == State.LEARNING)).hasSize(4);
        assertThat(selected.stream().filter(c -> c.state() == State.NEW)).hasSize(2);
    }

    @Test
    @DisplayName("Falls back to remaining candidates when a state bucket is short")
    void fallsBackWhenBucketShort() {
        var rotationHour = Instant.parse("2026-01-01T10:00:00Z");
        var candidates = List.of(
                candidate("c1", "v1", State.REVIEW, "2026-01-01T00:00:00Z"),
                candidate("c2", "v2", State.REVIEW, "2026-01-01T00:01:00Z"),
                candidate("c3", "v3", State.NEW, "2026-01-01T00:02:00Z"),
                candidate("c4", "v4", State.NEW, "2026-01-01T00:03:00Z"),
                candidate("c5", "v5", State.NEW, "2026-01-01T00:04:00Z")
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(5);
        assertThat(selected.stream().map(WritingPracticeCandidate::flashCardId).distinct()).hasSize(5);
    }

    private WritingPracticeCandidate candidate(String cardId, String vocabId, State state, String createdAt) {
        return new WritingPracticeCandidate(cardId, vocabId, state, Instant.parse(createdAt));
    }
}
