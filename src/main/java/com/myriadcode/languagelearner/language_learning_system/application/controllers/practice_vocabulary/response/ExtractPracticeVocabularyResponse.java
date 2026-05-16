package com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.response;

import java.util.List;

public record ExtractPracticeVocabularyResponse(
        int addedCount,
        int existingCount,
        List<String> matchedWords,
        List<String> vocabularyIds
) {
}
