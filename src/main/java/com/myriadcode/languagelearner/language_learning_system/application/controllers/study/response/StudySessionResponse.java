package com.myriadcode.languagelearner.language_learning_system.application.controllers.study.response;

import com.myriadcode.fsrs.api.enums.Rating;

import java.time.Instant;

public record StudySessionResponse(
        String sessionId,
        String status,
        int ratedCount,
        int totalCount,
        Item currentItem,
        Instant createdAt,
        String feedback,
        Rating appliedRating
) {
    public record Item(
            String itemId,
            String flashcardId,
            String vocabularyId,
            String sentenceId,
            String clozeSentence,
            String hint,
            String expectedAnswer,
            String answerTranslation
    ) {}
}
