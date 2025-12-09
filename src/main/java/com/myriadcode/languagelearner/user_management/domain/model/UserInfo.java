package com.myriadcode.languagelearner.user_management.domain.model;

import com.myriadcode.languagelearner.common.ids.UserId;
import jakarta.annotation.Nullable;

public record UserInfo(@Nullable UserId id, String username, String password) {

}
