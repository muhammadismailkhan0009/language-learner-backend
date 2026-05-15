package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model;

import java.time.Instant;
import java.util.List;

public record ReadingParagraphClozeParagraph(
        ReadingParagraphClozeParagraphId id,
        int paragraphIndex,
        String scenarioLabel,
        String clozeParagraph,
        Instant createdAt,
        List<ReadingParagraphClozeCard> cards
) {
    public record ReadingParagraphClozeParagraphId(String id) {
    }
}

