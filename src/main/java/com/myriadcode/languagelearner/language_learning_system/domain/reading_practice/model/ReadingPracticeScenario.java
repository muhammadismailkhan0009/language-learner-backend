package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model;

import java.util.List;

public record ReadingPracticeScenario(ReadingPracticeScenarioId id, String label, String readingText,
                                      int position, List<ReadingPracticeParagraph> paragraphs,
                                      List<ReadingVocabularyUsage> vocabularyUsages) {
    public record ReadingPracticeScenarioId(String id) {}
}
