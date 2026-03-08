package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model;

public record WritingSentencePair(
        WritingSentencePairId id,
        String englishSentence,
        String germanSentence,
        int position
) {
    public record WritingSentencePairId(String id) {
    }
}
