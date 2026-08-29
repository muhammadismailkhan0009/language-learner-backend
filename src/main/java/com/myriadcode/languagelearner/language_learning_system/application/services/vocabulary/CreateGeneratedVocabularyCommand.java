package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.util.List;

public record CreateGeneratedVocabularyCommand(
        String surface, String translation, Vocabulary.EntryKind entryKind,
        String notes, List<ExampleSentence> exampleSentences
) {
    public record ExampleSentence(String sentence, String translation) {}
}
