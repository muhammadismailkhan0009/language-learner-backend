package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response;

import java.time.Instant;

public record ReadingPracticeSessionSummaryResponse(
        String sessionId,
        String topic,
        Instant createdAt,
        String readingTextPreview,
        int vocabCount
) {
}
