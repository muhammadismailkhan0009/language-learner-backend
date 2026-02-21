package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.util.List;

public record UpdateVocabularyRequest(
        String surface,
        String translation,
        Vocabulary.EntryKind entryKind,
        String notes,
        List<ExampleSentenceUpdateRequest> exampleSentences
) {

    public record ExampleSentenceUpdateRequest(String id, String sentence, String translation) {
    }
}
