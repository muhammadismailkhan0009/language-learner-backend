package com.myriadcode.languagelearner.user_management.domain.model;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.time.Instant;

public record UserProfile(
        String userId,
        LanguageLevel difficultyLevel,
        LanguageLevel readingDifficultyLevel,
        LanguageLevel writingDifficultyLevel,
        Instant createdAt,
        Instant updatedAt
) {

    public UserProfile(String userId, LanguageLevel difficultyLevel, Instant createdAt, Instant updatedAt) {
        this(userId, difficultyLevel, LanguageLevel.A1, LanguageLevel.A1, createdAt, updatedAt);
    }

    public static UserProfile create(String userId) {
        return new UserProfile(userId, LanguageLevel.defaultLevel(), null, null);
    }

    public UserProfile withPracticeDifficultyLevels(LanguageLevel readingLevel, LanguageLevel writingLevel) {
        return new UserProfile(userId, difficultyLevel, readingLevel, writingLevel, createdAt, updatedAt);
    }
}
