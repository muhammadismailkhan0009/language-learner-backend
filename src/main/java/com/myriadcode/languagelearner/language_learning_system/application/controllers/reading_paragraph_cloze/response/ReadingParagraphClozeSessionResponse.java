package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response;

import java.time.Instant;
import java.util.List;

public record ReadingParagraphClozeSessionResponse(
        String sessionId,
        String topic,
        String clozeParagraph,
        String status,
        int ratedCount,
        int totalCount,
        List<Card> cards,
        Instant createdAt
) {
    public record Card(
            String cardId,
            String flashcardId,
            String vocabularyId,
            String surface,
            String translation,
            String blankToken,
            List<String> answerWords
    ) {
    }
}
