package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response;

import java.time.Instant;
import java.util.List;

public record ReadingPracticeSessionResponse(
        String sessionId,
        String topic,
        String readingText,
        List<ReadingVocabularyFlashCardView> vocabFlashcards,
        Instant createdAt
) {
}
