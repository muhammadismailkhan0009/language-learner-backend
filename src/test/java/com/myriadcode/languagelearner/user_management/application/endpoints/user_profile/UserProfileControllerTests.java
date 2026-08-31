package com.myriadcode.languagelearner.user_management.application.endpoints.user_profile;

import com.myriadcode.languagelearner.user_management.application.endpoints.user_profile.response.UserProfileResponse;
import com.myriadcode.languagelearner.user_management.application.services.UserProfileService;
import com.myriadcode.languagelearner.user_management.domain.repo.UserProfileRepo;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProfileControllerTests {

    private final UserProfileResponse profile = new UserProfileResponse(
                "user-1", "A2", "B1", "B2",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
    );
    private final RecordingUserProfileService userProfileService = new RecordingUserProfileService(profile);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new UserProfileController(userProfileService)).build();

    @Test
    void returns_all_profile_levels() throws Exception {
        mvc.perform(get("/api/v1/users/me/profile").queryParam("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.difficultyLevel").value("A2"))
                .andExpect(jsonPath("$.response.readingDifficultyLevel").value("B1"))
                .andExpect(jsonPath("$.response.writingDifficultyLevel").value("B2"));
    }

    @Test
    void updates_all_profile_levels_atomically() throws Exception {
        mvc.perform(patch("/api/v1/users/me/profile/difficulty-level")
                        .queryParam("userId", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "difficultyLevel": "A2",
                                  "readingDifficultyLevel": "B1",
                                  "writingDifficultyLevel": "B2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.readingDifficultyLevel").value("B1"))
                .andExpect(jsonPath("$.response.writingDifficultyLevel").value("B2"));

        org.assertj.core.api.Assertions.assertThat(userProfileService.lastUpdate)
                .containsExactly("user-1", "A2", "B1", "B2");
    }

    @Test
    void keeps_existing_patch_request_compatible() throws Exception {
        mvc.perform(patch("/api/v1/users/me/profile/difficulty-level")
                        .queryParam("userId", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"difficultyLevel\":\"A2\"}"))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(userProfileService.lastUpdate)
                .containsExactly("user-1", "A2", null, null);
    }

    private static final class RecordingUserProfileService extends UserProfileService {
        private final UserProfileResponse profile;
        private String[] lastUpdate;

        private RecordingUserProfileService(UserProfileResponse profile) {
            super((UserProfileRepo) null);
            this.profile = profile;
        }

        @Override
        public UserProfileResponse getProfile(String userId) {
            return profile;
        }

        @Override
        public UserProfileResponse updateProfileLevels(
                String userId,
                String difficultyLevel,
                String readingDifficultyLevel,
                String writingDifficultyLevel
        ) {
            lastUpdate = new String[]{userId, difficultyLevel, readingDifficultyLevel, writingDifficultyLevel};
            return profile;
        }
    }
}
