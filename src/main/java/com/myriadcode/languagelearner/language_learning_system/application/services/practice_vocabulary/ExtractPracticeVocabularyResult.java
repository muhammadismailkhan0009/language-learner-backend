package com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary;

import java.util.List;

public record ExtractPracticeVocabularyResult(
        int addedCount,
        int existingCount,
        List<String> matchedWords,
        List<String> vocabularyIds
) {
}

