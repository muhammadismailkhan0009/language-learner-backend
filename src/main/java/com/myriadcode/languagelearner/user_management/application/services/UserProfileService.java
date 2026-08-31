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
                existing.readingDifficultyLevel(),
                existing.writingDifficultyLevel(),
                existing.createdAt(),
                existing.updatedAt()
        ));
        return toResponse(updated);
    }

    @Transactional
    public UserProfileResponse updatePracticeDifficultyLevels(
            String userId,
            String readingDifficultyLevel,
            String writingDifficultyLevel
    ) {
        var existing = getOrCreateProfile(requireUserId(userId));
        var updated = existing.withPracticeDifficultyLevels(
                LanguageLevel.from(readingDifficultyLevel),
                LanguageLevel.from(writingDifficultyLevel)
        );
        return toResponse(userProfileRepo.save(updated));
    }

    @Transactional
    public UserProfileResponse updateProfileLevels(
            String userId,
            String difficultyLevel,
            String readingDifficultyLevel,
            String writingDifficultyLevel
    ) {
        var existing = getOrCreateProfile(requireUserId(userId));
        var updated = new UserProfile(
                existing.userId(),
                difficultyLevel == null ? existing.difficultyLevel() : LanguageLevel.from(difficultyLevel),
                readingDifficultyLevel == null
                        ? existing.readingDifficultyLevel()
                        : LanguageLevel.from(readingDifficultyLevel),
                writingDifficultyLevel == null
                        ? existing.writingDifficultyLevel()
                        : LanguageLevel.from(writingDifficultyLevel),
                existing.createdAt(),
                existing.updatedAt()
        );
        return toResponse(userProfileRepo.save(updated));
    }

    @Transactional
    public LanguageLevel getReadingDifficultyLevel(String userId) {
        return getOrCreateProfile(requireUserId(userId)).readingDifficultyLevel();
    }

    @Transactional
    public LanguageLevel getWritingDifficultyLevel(String userId) {
        return getOrCreateProfile(requireUserId(userId)).writingDifficultyLevel();
    }

    private UserProfile getOrCreateProfile(String userId) {
        return userProfileRepo.findByUserId(userId)
                .orElseGet(() -> userProfileRepo.save(UserProfile.create(userId)));
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
                profile.readingDifficultyLevel().name(),
                profile.writingDifficultyLevel().name(),
                profile.createdAt(),
                profile.updatedAt()
        );
    }
}
