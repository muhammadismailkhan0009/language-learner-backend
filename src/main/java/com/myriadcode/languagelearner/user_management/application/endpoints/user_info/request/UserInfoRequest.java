package com.myriadcode.languagelearner.user_management.application.endpoints.user_info.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Valid
public record UserInfoRequest(@NotNull String username, @NotNull String password) {
}
