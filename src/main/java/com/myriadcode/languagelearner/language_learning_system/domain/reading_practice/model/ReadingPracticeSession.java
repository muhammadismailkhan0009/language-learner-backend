package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record ReadingPracticeSession(
        ReadingPracticeSessionId id,
        UserId userId,
        String topic,
        String readingText,
        List<ReadingPracticeParagraph> paragraphs,
        Instant createdAt,
        List<ReadingVocabularyUsage> vocabularyUsages,
        List<ReadingPracticeScenario> scenarios
) {

    public ReadingPracticeSession(ReadingPracticeSessionId id, UserId userId, String topic, String readingText,
                                  List<ReadingPracticeParagraph> paragraphs, Instant createdAt,
                                  List<ReadingVocabularyUsage> vocabularyUsages) {
        this(id, userId, topic, readingText, paragraphs, createdAt, vocabularyUsages,
                List.of(new ReadingPracticeScenario(
                        new ReadingPracticeScenario.ReadingPracticeScenarioId(id.id() + "-scenario"),
                        topic, readingText, 0, paragraphs, vocabularyUsages)));
    }

    public record ReadingPracticeSessionId(String id) {
    }
}
