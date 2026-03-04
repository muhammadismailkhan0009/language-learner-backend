package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model;

public record ReadingVocabularyUsage(
        ReadingVocabularyUsageId id,
        String flashCardId,
        String vocabularyId
) {

    public record ReadingVocabularyUsageId(String id) {
    }
}
