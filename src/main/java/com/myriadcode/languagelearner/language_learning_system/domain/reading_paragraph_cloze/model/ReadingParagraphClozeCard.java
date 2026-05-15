package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model;

import java.time.Instant;

public record ReadingParagraphClozeCard(
        ReadingParagraphClozeCardId id,
        String paragraphId,
        String flashcardId,
        String vocabularyId,
        Instant createdAt
) {
    public record ReadingParagraphClozeCardId(String id) {
    }
}
