package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model;

public record ReadingPracticeSentence(
        ReadingPracticeSentenceId id,
        String text,
        int position
) {
    public record ReadingPracticeSentenceId(String id) {
    }
}
