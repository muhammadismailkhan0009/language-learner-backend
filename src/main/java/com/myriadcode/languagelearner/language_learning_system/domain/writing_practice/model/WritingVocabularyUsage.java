package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model;

public record WritingVocabularyUsage(
        WritingVocabularyUsageId id,
        String flashCardId,
        String vocabularyId
) {

    public record WritingVocabularyUsageId(String id) {
    }
}
