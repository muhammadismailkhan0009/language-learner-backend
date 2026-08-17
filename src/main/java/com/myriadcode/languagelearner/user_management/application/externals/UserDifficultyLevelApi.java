package com.myriadcode.languagelearner.user_management.application.externals;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

public interface UserDifficultyLevelApi {

    LanguageLevel getDifficultyLevel(String userId);
}
