package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record ReadingParagraphClozeSession(
        ReadingParagraphClozeSessionId id,
        UserId userId,
        LanguageLevel learnerLevel,
        Instant createdAt,
        List<ReadingParagraphClozeParagraph> paragraphs
) {
    public ReadingParagraphClozeSession {
        paragraphs = paragraphs == null ? List.of() : List.copyOf(paragraphs);
    }
    public record ReadingParagraphClozeSessionId(String id) {
    }
}
