package com.myriadcode.languagelearner.user_management.domain.model;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.time.Instant;

public record UserProfile(
        String userId,
        LanguageLevel difficultyLevel,
        Instant createdAt,
        Instant updatedAt
) {
}
