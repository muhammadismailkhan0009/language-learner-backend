package com.myriadcode.languagelearner.behavior.user_profile;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.user_management.application.services.UserProfileService;
import com.myriadcode.languagelearner.user_management.domain.model.UserProfile;
import com.myriadcode.languagelearner.user_management.domain.repo.UserProfileRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileServiceBehaviorTests {

    @Test
    @DisplayName("getProfile: creates missing profile with A1 default")
    void getProfileCreatesMissingProfile() {
        var service = new UserProfileService(new InMemoryUserProfileRepo());

        var profile = service.getProfile("user-1");

        assertThat(profile.userId()).isEqualTo("user-1");
        assertThat(profile.difficultyLevel()).isEqualTo("A1");
        assertThat(profile.createdAt()).isNotNull();
        assertThat(profile.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("updateDifficultyLevel: lazily creates missing profile and saves selected enum level")
    void updateDifficultyLevelCreatesMissingProfile() {
        var repo = new InMemoryUserProfileRepo();
        var service = new UserProfileService(repo);

        var profile = service.updateDifficultyLevel("user-1", "b2");

        assertThat(profile.difficultyLevel()).isEqualTo("B2");
        assertThat(repo.findByUserId("user-1").orElseThrow().difficultyLevel()).isEqualTo(LanguageLevel.B2);
    }

    @Test
    @DisplayName("updateDifficultyLevel: rejects unsupported levels")
    void updateDifficultyLevelRejectsUnsupportedLevels() {
        var service = new UserProfileService(new InMemoryUserProfileRepo());

        assertThatThrownBy(() -> service.updateDifficultyLevel("user-1", "A1+"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported language level");
    }

    private static final class InMemoryUserProfileRepo implements UserProfileRepo {
        private final Map<String, UserProfile> data = new HashMap<>();

        @Override
        public Optional<UserProfile> findByUserId(String userId) {
            return Optional.ofNullable(data.get(userId));
        }

        @Override
        public UserProfile save(UserProfile profile) {
            var now = Instant.now();
            var saved = new UserProfile(
                    profile.userId(),
                    profile.difficultyLevel(),
                    profile.createdAt() == null ? now : profile.createdAt(),
                    now
            );
            data.put(saved.userId(), saved);
            return saved;
        }
    }
}
