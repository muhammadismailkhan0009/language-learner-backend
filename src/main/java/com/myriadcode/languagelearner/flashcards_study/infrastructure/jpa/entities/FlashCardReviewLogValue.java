package com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities;

import com.myriadcode.fsrs.api.enums.ReviewLogRating;
import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;

public record FlashCardReviewLogValue(
        double difficulty,
        Instant due,
        int elapsedDays,
        int lastElapsedDays,
        int learningSteps,
        ReviewLogRating rating,
        Instant review,
        int scheduledDays,
        double stability,
        State state
) {
}
