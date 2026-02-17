package com.myriadcode.languagelearner.language_learning_system.domain.listening.model;

import com.myriadcode.languagelearner.common.ids.UserId;

public record WordToListenTo(UserId userId, String word) {
}
