package com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.time.Instant;

public record PublicVocabulary(
        PublicVocabularyId id,
        Vocabulary.VocabularyId sourceVocabularyId,
        UserId publishedByUserId,
        PublicVocabularyStatus status,
        Instant publishedAt
) {

    public record PublicVocabularyId(String id) {
    }

    public enum PublicVocabularyStatus {
        PUBLISHED,
        UNPUBLISHED
    }
}
