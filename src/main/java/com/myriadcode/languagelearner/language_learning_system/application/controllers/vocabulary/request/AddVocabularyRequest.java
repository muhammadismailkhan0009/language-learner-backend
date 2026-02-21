package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.util.List;

public record AddVocabularyRequest(
        String surface,
        String translation,
        Vocabulary.EntryKind entryKind,
        String notes,
        List<ExampleSentenceRequest> exampleSentences
) {

    public record ExampleSentenceRequest(String sentence, String translation) {
    }
}
