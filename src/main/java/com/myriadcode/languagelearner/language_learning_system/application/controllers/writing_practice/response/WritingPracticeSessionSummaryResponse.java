package com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response;

import java.time.Instant;

public record WritingPracticeSessionSummaryResponse(
        String sessionId,
        String topic,
        Instant createdAt,
        String englishParagraphPreview,
        int vocabCount,
        boolean submitted
) {
}
