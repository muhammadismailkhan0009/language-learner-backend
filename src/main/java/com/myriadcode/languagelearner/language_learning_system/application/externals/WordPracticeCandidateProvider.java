package com.myriadcode.languagelearner.language_learning_system.application.externals;

import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;

import java.util.List;
import java.util.Map;

public interface WordPracticeCandidateProvider {
    Map<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>> findRankedCandidates(String userId);
}
