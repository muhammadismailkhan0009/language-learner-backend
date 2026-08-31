package com.myriadcode.languagelearner.user_management.application.endpoints.user_profile;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.user_management.application.endpoints.user_profile.request.UpdateUserDifficultyLevelRequest;
import com.myriadcode.languagelearner.user_management.application.endpoints.user_profile.response.UserProfileResponse;
import com.myriadcode.languagelearner.user_management.application.services.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/users/me/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(new ApiResponse<>(userProfileService.getProfile(userId)));
    }

    @PatchMapping("difficulty-level")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateDifficultyLevel(
            @RequestParam("userId") String userId,
            @RequestBody UpdateUserDifficultyLevelRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(userProfileService.updateProfileLevels(
                userId,
                request.difficultyLevel(),
                request.readingDifficultyLevel(),
                request.writingDifficultyLevel()
        )));
    }

}
