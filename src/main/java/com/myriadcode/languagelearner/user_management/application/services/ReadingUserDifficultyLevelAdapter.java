package com.myriadcode.languagelearner.user_management.application.services;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import org.springframework.stereotype.Service;

@Service
public class ReadingUserDifficultyLevelAdapter implements UserDifficultyLevelApi {

    private final UserProfileService userProfileService;

    public ReadingUserDifficultyLevelAdapter(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public LanguageLevel getDifficultyLevel(String userId) {
        return LanguageLevel.from(userProfileService.getProfile(userId).difficultyLevel());
    }
}
