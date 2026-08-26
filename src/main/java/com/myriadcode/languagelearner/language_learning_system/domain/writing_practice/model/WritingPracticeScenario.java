package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model;

import java.time.Instant;
import java.util.List;

public record WritingPracticeScenario(
        WritingPracticeScenarioId id,
        int position,
        String topic,
        String englishParagraph,
        String germanParagraph,
        String submittedAnswer,
        Instant submittedAt,
        String feedbackText,
        WritingStructuredFeedback structuredFeedback,
        Instant feedbackGeneratedAt,
        List<WritingSentencePair> sentencePairs,
        List<WritingVocabularyUsage> vocabularyUsages
) {
    public WritingPracticeScenario {
        sentencePairs = sentencePairs == null ? List.of() : List.copyOf(sentencePairs);
        vocabularyUsages = vocabularyUsages == null ? List.of() : List.copyOf(vocabularyUsages);
    }

    public record WritingPracticeScenarioId(String id) {
    }
}
