package com.myriadcode.languagelearner.user_management.application.services;


import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.user_management.application.endpoints.user_info.request.UserInfoRequest;
import com.myriadcode.languagelearner.user_management.application.externals.UserInformationApi;
import com.myriadcode.languagelearner.user_management.application.mappers.UserInfoMapper;
import com.myriadcode.languagelearner.user_management.domain.repo.UserInfoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRegistrationService implements UserInformationApi {

    @Autowired
    private UserInfoRepo userInfoRepo;

    public UserId registerUser(UserInfoRequest userInfoToRegister) {

        var existingUser = userInfoRepo.findByUserName(userInfoToRegister.username());
        if (existingUser.isEmpty()) {
            var userInfo = UserInfoMapper.INSTANCE.toDomain(userInfoToRegister);
            var saved = userInfoRepo.save(userInfo);
            return saved.id();
        }
        return existingUser.get().id();
    }

    @Override
    public List<String> userIds() {
        var users = userInfoRepo.findAll();
        return users.parallelStream().map(user -> user.id().id()).toList();
    }
}
