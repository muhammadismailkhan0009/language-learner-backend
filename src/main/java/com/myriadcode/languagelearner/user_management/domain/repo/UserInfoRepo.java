package com.myriadcode.languagelearner.user_management.domain.repo;

import com.myriadcode.languagelearner.user_management.domain.model.UserInfo;

import java.util.List;
import java.util.Optional;

public interface UserInfoRepo {

    Optional<UserInfo> findByUserName(String username);

    UserInfo save(UserInfo userInfo);

    List<UserInfo> findAll();
}
