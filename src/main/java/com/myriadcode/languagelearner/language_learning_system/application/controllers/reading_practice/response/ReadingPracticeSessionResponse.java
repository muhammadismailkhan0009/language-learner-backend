package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response;

import java.time.Instant;
import java.util.List;

public record ReadingPracticeSessionResponse(
        String sessionId,
        String topic,
        String readingText,
        List<ReadingPracticeParagraphResponse> readingParagraphs,
        List<ReadingVocabularyFlashCardView> vocabFlashcards,
        Instant createdAt,
        List<ReadingPracticeScenarioResponse> scenarios
) {
    public ReadingPracticeSessionResponse(String sessionId, String topic, String readingText,
                                          List<ReadingPracticeParagraphResponse> readingParagraphs,
                                          List<ReadingVocabularyFlashCardView> vocabFlashcards,
                                          Instant createdAt) {
        this(sessionId, topic, readingText, readingParagraphs, vocabFlashcards, createdAt, List.of());
    }
}
