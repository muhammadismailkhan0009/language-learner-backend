package com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;

@Deprecated(forRemoval = true)
public record PracticeVocabularyExtractionRequest(UserId userId, String text, Instant createdAt) {
}
