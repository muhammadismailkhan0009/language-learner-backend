package com.myriadcode.languagelearner.language_learning_system.reading_and_listening.domain.listening;

import com.myriadcode.languagelearner.common.ids.UserId;

public record WordToListenTo(UserId userId, String word) {
}
