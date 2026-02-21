package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.util.List;

public record VocabularyResponse(
        String id,
        String userId,
        String surface,
        String translation,
        Vocabulary.EntryKind entryKind,
        String notes,
        List<ExampleSentenceResponse> exampleSentences
) {

    public record ExampleSentenceResponse(String id, String sentence, String translation) {
    }
}
