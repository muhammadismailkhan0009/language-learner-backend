package com.myriadcode.languagelearner.flashcards_study.domain.models;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.List;

public record FsrsRescheduleResult(
        FsrsCard card,
        List<ReviewLog> reviewLogs
) {

    public static FsrsRescheduleResult withCard(FsrsCard card) {
        return new FsrsRescheduleResult(card, List.of());
    }

    public double difficulty() {
        return card.difficulty();
    }

    public Instant due() {
        return card.due();
    }

    public int elapsedDays() {
        return card.elapsedDays();
    }

    public int lapses() {
        return card.lapses();
    }

    public Instant lastReview() {
        return card.lastReview();
    }

    public int learningSteps() {
        return card.learningSteps();
    }

    public int reps() {
        return card.reps();
    }

    public int scheduledDays() {
        return card.scheduledDays();
    }

    public double stability() {
        return card.stability();
    }

    public State state() {
        return card.state();
    }
}
