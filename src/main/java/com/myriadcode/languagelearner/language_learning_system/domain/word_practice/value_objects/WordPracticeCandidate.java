package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects;

import java.util.Objects;

public record WordPracticeCandidate(
        String vocabularyId,
        WordPracticeSelectionCategory category,
        String weakEventId
) {
    public WordPracticeCandidate {
        if (vocabularyId == null || vocabularyId.isBlank()) {
            throw new IllegalArgumentException("vocabularyId must not be blank");
        }
        Objects.requireNonNull(category, "category must not be null");
        if (category == WordPracticeSelectionCategory.WEAK && (weakEventId == null || weakEventId.isBlank())) {
            throw new IllegalArgumentException("weakEventId must identify the weak event");
        }
    }
}
