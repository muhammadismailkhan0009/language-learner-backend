package com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice.response;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;

import java.util.List;

public record WordPracticeQueueResponse(
        int activeVocabularyCount,
        int maximumVocabularyCount,
        int generationVocabularyCount,
        List<WordPractice> practices
) {
}
