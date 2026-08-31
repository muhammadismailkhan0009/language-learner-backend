package com.myriadcode.languagelearner.user_management.domain.model;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileTests {

    @Test
    void creates_profile_with_independent_a1_practice_levels() {
        var profile = UserProfile.create("user-1");

        assertThat(profile)
                .extracting(
                        UserProfile::difficultyLevel,
                        UserProfile::readingDifficultyLevel,
                        UserProfile::writingDifficultyLevel
                )
                .containsExactly(LanguageLevel.defaultLevel(), LanguageLevel.A1, LanguageLevel.A1);
    }

    @Test
    void changes_practice_levels_without_changing_unified_level() {
        var profile = new UserProfile(
                "user-1",
                LanguageLevel.B1,
                LanguageLevel.A1,
                LanguageLevel.A1,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );

        var updated = profile.withPracticeDifficultyLevels(LanguageLevel.B2, LanguageLevel.C1);

        assertThat(updated)
                .extracting(
                        UserProfile::difficultyLevel,
                        UserProfile::readingDifficultyLevel,
                        UserProfile::writingDifficultyLevel
                )
                .containsExactly(LanguageLevel.B1, LanguageLevel.B2, LanguageLevel.C1);
    }
}
