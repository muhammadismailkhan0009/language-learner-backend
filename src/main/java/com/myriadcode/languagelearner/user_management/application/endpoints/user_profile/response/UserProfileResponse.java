package com.myriadcode.languagelearner.user_management.application.endpoints.user_profile.response;

import java.time.Instant;

public record UserProfileResponse(
        String userId,
        String difficultyLevel,
        Instant createdAt,
        Instant updatedAt
) {
}
