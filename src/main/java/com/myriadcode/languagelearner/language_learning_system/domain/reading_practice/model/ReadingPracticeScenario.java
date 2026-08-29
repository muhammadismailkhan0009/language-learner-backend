package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model;

import java.util.List;

public record ReadingPracticeScenario(ReadingPracticeScenarioId id, String label, String readingText,
                                      int position, List<ReadingPracticeParagraph> paragraphs,
                                      List<ReadingVocabularyUsage> vocabularyUsages,
                                      int ratedCardsCount, boolean allCardsRated) {
    public ReadingPracticeScenario(ReadingPracticeScenarioId id, String label, String readingText,
                                   int position, List<ReadingPracticeParagraph> paragraphs,
                                   List<ReadingVocabularyUsage> vocabularyUsages) {
        this(id, label, readingText, position, paragraphs, vocabularyUsages, 0, false);
    }

    public ReadingPracticeScenario {
        if (ratedCardsCount < 0) throw new IllegalArgumentException("Rated cards count cannot be negative");
    }

    public RatingProgress recordAcceptedRating() {
        int totalCards = vocabularyUsages == null ? 0 : vocabularyUsages.size();
        if (allCardsRated || totalCards == 0) return new RatingProgress(this, false);
        int nextCount = Math.min(ratedCardsCount + 1, totalCards);
        boolean completedNow = nextCount == totalCards;
        return new RatingProgress(new ReadingPracticeScenario(
                id, label, readingText, position, paragraphs, vocabularyUsages,
                nextCount, completedNow), completedNow);
    }

    public record RatingProgress(ReadingPracticeScenario scenario, boolean completedNow) {}

    public record ReadingPracticeScenarioId(String id) {}
}
