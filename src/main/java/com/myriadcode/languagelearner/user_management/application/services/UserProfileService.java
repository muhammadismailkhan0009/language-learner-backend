package com.myriadcode.languagelearner.user_management.application.services;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.user_management.application.endpoints.user_profile.response.UserProfileResponse;
import com.myriadcode.languagelearner.user_management.domain.model.UserProfile;
import com.myriadcode.languagelearner.user_management.domain.repo.UserProfileRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepo userProfileRepo;

    public UserProfileService(UserProfileRepo userProfileRepo) {
        this.userProfileRepo = userProfileRepo;
    }

    @Transactional
    public UserProfileResponse getProfile(String userId) {
        return toResponse(getOrCreateProfile(requireUserId(userId)));
    }

    @Transactional
    public UserProfileResponse updateDifficultyLevel(String userId, String difficultyLevel) {
        var normalizedUserId = requireUserId(userId);
        var selectedLevel = LanguageLevel.from(difficultyLevel);
        var existing = getOrCreateProfile(normalizedUserId);
        var updated = userProfileRepo.save(new UserProfile(
                existing.userId(),
                selectedLevel,
                existing.createdAt(),
                existing.updatedAt()
        ));
        return toResponse(updated);
    }

    private UserProfile getOrCreateProfile(String userId) {
        return userProfileRepo.findByUserId(userId)
                .orElseGet(() -> userProfileRepo.save(new UserProfile(
                        userId,
                        LanguageLevel.defaultLevel(),
                        null,
                        null
                )));
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
        return userId.trim();
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.userId(),
                profile.difficultyLevel().name(),
                profile.createdAt(),
                profile.updatedAt()
        );
    }
}
