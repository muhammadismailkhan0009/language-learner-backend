package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;

import java.time.Instant;
import java.util.List;

public record WordPractice(
        String id,
        String userId,
        String vocabularyId,
        WordPracticeDirection direction,
        String sourceSentence,
        String clozeSentence,
        String completeSentence,
        String exactAnswer,
        List<String> acceptedAnswers,
        Instant createdAt
) {
    public WordPractice {
        acceptedAnswers = acceptedAnswers == null ? List.of() : List.copyOf(acceptedAnswers);
    }

    public boolean isCorrect(String submittedAnswer) {
        if (submittedAnswer == null || submittedAnswer.isBlank()) {
            return false;
        }
        var normalized = submittedAnswer.strip();
        return exactAnswer.equalsIgnoreCase(normalized)
                || acceptedAnswers.stream().anyMatch(answer -> answer.equalsIgnoreCase(normalized));
    }
}
