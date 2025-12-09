package com.myriadcode.languagelearner.user_management.application.endpoints;

import jakarta.validation.constraints.NotNull;

public record ApiResponse<T>(@NotNull T response) {
}
