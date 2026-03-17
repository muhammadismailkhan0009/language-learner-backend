package com.myriadcode.languagelearner.user_management.application.externals;

import java.util.List;
import java.util.Optional;

public interface UserInformationApi {
    List<String> userIds();

    Optional<String> findUsernameByUserId(String userId);
}
