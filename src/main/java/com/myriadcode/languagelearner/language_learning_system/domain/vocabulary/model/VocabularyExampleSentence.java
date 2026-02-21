package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model;

public record VocabularyExampleSentence(
        VocabularyExampleSentenceId id,
        String sentence,
        String translation
) {

    public record VocabularyExampleSentenceId(String id) {
    }
}
