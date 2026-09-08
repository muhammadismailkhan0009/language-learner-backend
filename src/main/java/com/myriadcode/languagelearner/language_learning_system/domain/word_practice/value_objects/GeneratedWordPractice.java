package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects;

import java.util.List;

public record GeneratedWordPractice(
        String vocabularyId,
        WordPracticeDirection direction,
        String sourceSentence,
        String clozeSentence,
        String completeSentence,
        String exactAnswer,
        List<String> acceptedAnswers
) {
    public GeneratedWordPractice {
        acceptedAnswers = acceptedAnswers == null ? List.of() : List.copyOf(acceptedAnswers);
    }
}
