package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record ReadingPracticeSession(
        ReadingPracticeSessionId id,
        UserId userId,
        String topic,
        String readingText,
        Instant createdAt,
        List<ReadingVocabularyUsage> vocabularyUsages
) {

    public record ReadingPracticeSessionId(String id) {
    }
}
