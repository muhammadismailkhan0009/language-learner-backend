package com.myriadcode.languagelearner.user_management.application.externals;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

public interface UserDifficultyLevelApi {

    LanguageLevel getDifficultyLevel(String userId);

    default LanguageLevel getReadingDifficultyLevel(String userId) {
        return getDifficultyLevel(userId);
    }

    default LanguageLevel getWritingDifficultyLevel(String userId) {
        return getDifficultyLevel(userId);
    }
}
