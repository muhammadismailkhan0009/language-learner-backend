package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;

public record WritingPracticeCandidate(
        String flashCardId,
        String vocabularyId,
        State state,
        Instant vocabularyCreatedAt,
        Instant due,
        double stability,
        double difficulty,
        int lapses,
        Instant lastReview
) {
}
