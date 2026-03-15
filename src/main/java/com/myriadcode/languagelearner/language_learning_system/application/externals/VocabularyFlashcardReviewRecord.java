package com.myriadcode.languagelearner.language_learning_system.application.externals;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;

public record VocabularyFlashcardReviewRecord(
        String flashcardId,
        String vocabularyId,
        State fsrsState,
        Instant due,
        double retrievability,
        double stability,
        double difficulty,
        int lapses,
        Instant lastReview,
        boolean isReversed
) {

    public VocabularyFlashcardReviewRecord(
            String flashcardId,
            String vocabularyId,
            State fsrsState,
            Instant due,
            double stability,
            double difficulty,
            int lapses,
            Instant lastReview,
            boolean isReversed
    ) {
        this(flashcardId, vocabularyId, fsrsState, due, Double.NaN, stability, difficulty, lapses, lastReview, isReversed);
    }

    public VocabularyFlashcardReviewRecord(
            String flashcardId,
            String vocabularyId,
            State fsrsState,
            boolean isReversed
    ) {
        this(flashcardId, vocabularyId, fsrsState, null, Double.NaN, 0.0, 0.0, 0, null, isReversed);
    }
}
