package com.myriadcode.languagelearner.user_management.application.endpoints.user_profile.request;

public record UpdateUserDifficultyLevelRequest(
        String difficultyLevel,
        String readingDifficultyLevel,
        String writingDifficultyLevel
) {
}
