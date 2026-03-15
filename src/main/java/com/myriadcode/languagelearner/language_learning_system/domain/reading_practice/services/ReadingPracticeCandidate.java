package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;

public record ReadingPracticeCandidate(
        String flashCardId,
        String vocabularyId,
        State state,
        Instant vocabularyCreatedAt,
        Instant due,
        double retrievability,
        int lapses,
        Instant lastReview
) {
}
