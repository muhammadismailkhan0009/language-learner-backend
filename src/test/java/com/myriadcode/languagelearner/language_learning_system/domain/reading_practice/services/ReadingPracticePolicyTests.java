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
    @DisplayName("Selects exact ratio when enough candidates exist per state")
    void selectsExactRatio() {
        var rotationHour = Instant.parse("2026-01-01T10:00:00Z");
        var candidates = List.of(
                candidate("c1", "v1", State.REVIEW, "2026-01-01T00:00:00Z"),
                candidate("c2", "v2", State.REVIEW, "2026-01-01T00:01:00Z"),
                candidate("c3", "v3", State.REVIEW, "2026-01-01T00:02:00Z"),
                candidate("c4", "v4", State.REVIEW, "2026-01-01T00:03:00Z"),
                candidate("c5", "v5", State.REVIEW, "2026-01-01T00:04:00Z"),
                candidate("c6", "v6", State.REVIEW, "2026-01-01T00:05:00Z"),
                candidate("c7", "v7", State.RE_LEARNING, "2026-01-01T00:06:00Z"),
                candidate("c8", "v8", State.RE_LEARNING, "2026-01-01T00:07:00Z"),
                candidate("c9", "v9", State.RE_LEARNING, "2026-01-01T00:08:00Z"),
                candidate("c10", "v10", State.RE_LEARNING, "2026-01-01T00:09:00Z"),
                candidate("c11", "v11", State.RE_LEARNING, "2026-01-01T00:10:00Z"),
                candidate("c12", "v12", State.RE_LEARNING, "2026-01-01T00:11:00Z"),
                candidate("c13", "v13", State.RE_LEARNING, "2026-01-01T00:12:00Z"),
                candidate("c14", "v14", State.RE_LEARNING, "2026-01-01T00:13:00Z"),
                candidate("c15", "v15", State.LEARNING, "2026-01-01T00:14:00Z"),
                candidate("c16", "v16", State.LEARNING, "2026-01-01T00:15:00Z"),
                candidate("c17", "v17", State.LEARNING, "2026-01-01T00:16:00Z"),
                candidate("c18", "v18", State.LEARNING, "2026-01-01T00:17:00Z"),
                candidate("c19", "v19", State.NEW, "2026-01-01T00:18:00Z"),
                candidate("c20", "v20", State.NEW, "2026-01-01T00:19:00Z")
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(20);
        assertThat(selected.stream().filter(c -> c.state() == State.REVIEW)).hasSize(6);
        assertThat(selected.stream().filter(c -> c.state() == State.RE_LEARNING)).hasSize(8);
        assertThat(selected.stream().filter(c -> c.state() == State.LEARNING)).hasSize(4);
        assertThat(selected.stream().filter(c -> c.state() == State.NEW)).hasSize(2);
        assertThat(selected.stream().map(ReadingPracticeCandidate::flashCardId).distinct()).hasSize(20);
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
                candidate("c5", "v5", State.NEW, "2026-01-01T00:04:00Z"),
                candidate("c6", "v6", State.NEW, "2026-01-01T00:05:00Z"),
                candidate("c7", "v7", State.NEW, "2026-01-01T00:06:00Z"),
                candidate("c8", "v8", State.NEW, "2026-01-01T00:07:00Z")
        );

        var selected = policy.selectCandidates("user-1", candidates, rotationHour);

        assertThat(selected).hasSize(8);
        assertThat(selected.stream().map(ReadingPracticeCandidate::flashCardId).distinct()).hasSize(8);
    }

    @Test
    @DisplayName("Selects 20 with preserved 3/4/2/1 ratio when enough candidates exist")
    void selectsTwentyWithPreservedRatio() {
        var rotationHour = Instant.parse("2026-01-01T10:00:00Z");
        var candidates = new java.util.ArrayList<ReadingPracticeCandidate>();

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

    private ReadingPracticeCandidate candidate(String cardId, String vocabId, State state, String createdAt) {
        return new ReadingPracticeCandidate(
                cardId,
                vocabId,
                state,
                Instant.parse(createdAt)
        );
    }
}
