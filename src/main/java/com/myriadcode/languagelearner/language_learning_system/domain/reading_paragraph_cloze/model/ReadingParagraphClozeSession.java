package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record ReadingParagraphClozeSession(
        ReadingParagraphClozeSessionId id,
        UserId userId,
        String topic,
        String clozeParagraph,
        Instant createdAt,
        List<ReadingParagraphClozeCard> cards
) {
    public record ReadingParagraphClozeSessionId(String id) {
    }

    public int totalCount() {
        return cards == null ? 0 : cards.size();
    }
}
