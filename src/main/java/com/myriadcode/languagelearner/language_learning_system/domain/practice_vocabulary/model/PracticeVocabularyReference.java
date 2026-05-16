package com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.time.Instant;

public record PracticeVocabularyReference(
        PracticeVocabularyReferenceId id,
        UserId userId,
        Vocabulary.VocabularyId vocabularyId,
        int timesMatched,
        Instant createdAt,
        Instant updatedAt
) {
    public record PracticeVocabularyReferenceId(String id) {
    }
}
