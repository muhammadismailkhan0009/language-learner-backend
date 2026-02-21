package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.util.List;

public record Vocabulary(
        VocabularyId id,
        UserId userId,
        String surface,
        String translation,
        EntryKind entryKind,
        String notes,
        List<VocabularyExampleSentence> exampleSentences
) {

    public record VocabularyId(String id) {
    }

    public enum EntryKind {
        WORD,
        CHUNK
    }
}
