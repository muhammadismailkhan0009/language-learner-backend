package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;

import java.util.Objects;

public record WordPracticeGenerationCandidate(
        WordPracticeCandidate selection,
        String german,
        String english
) {
    public WordPracticeGenerationCandidate {
        Objects.requireNonNull(selection, "selection must not be null");
        if (german == null || german.isBlank()) throw new IllegalArgumentException("german must not be blank");
        if (english == null || english.isBlank()) throw new IllegalArgumentException("english must not be blank");
    }

    public String vocabularyId() {
        return selection.vocabularyId();
    }
}
