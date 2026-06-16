package com.myriadcode.languagelearner.user_management.domain.repo;

import com.myriadcode.languagelearner.user_management.domain.model.UserProfile;

import java.util.Optional;

public interface UserProfileRepo {

    Optional<UserProfile> findByUserId(String userId);

    UserProfile save(UserProfile profile);
}
