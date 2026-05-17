package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model;

import java.time.Instant;

public record UniversalVocabularyPoolEntry(
        UniversalVocabularyPoolEntryId id,
        String normalizedSurface,
        String surface,
        Vocabulary.EntryKind entryKind,
        Instant createdAt,
        Instant updatedAt
) {
    public record UniversalVocabularyPoolEntryId(String id) {
    }
}
