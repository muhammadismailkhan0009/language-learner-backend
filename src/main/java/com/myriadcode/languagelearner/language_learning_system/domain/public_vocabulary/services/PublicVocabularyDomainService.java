package com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.services;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.time.Instant;
import java.util.UUID;

public final class PublicVocabularyDomainService {

    private PublicVocabularyDomainService() {
    }

    public static PublicVocabulary publish(Vocabulary.VocabularyId sourceVocabularyId, UserId publishedByUserId) {
        validateSourceVocabularyId(sourceVocabularyId);
        validatePublishedByUserId(publishedByUserId);

        return new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId(UUID.randomUUID().toString()),
                sourceVocabularyId,
                publishedByUserId,
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.now()
        );
    }

    public static PublicVocabulary ensurePublished(PublicVocabulary existing) {
        if (existing.status() == PublicVocabulary.PublicVocabularyStatus.PUBLISHED) {
            return existing;
        }
        return new PublicVocabulary(
                existing.id(),
                existing.sourceVocabularyId(),
                existing.publishedByUserId(),
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.now()
        );
    }

    private static void validateSourceVocabularyId(Vocabulary.VocabularyId sourceVocabularyId) {
        if (sourceVocabularyId == null || sourceVocabularyId.id() == null || sourceVocabularyId.id().isBlank()) {
            throw new IllegalArgumentException("Source vocabulary id is required");
        }
    }

    private static void validatePublishedByUserId(UserId publishedByUserId) {
        if (publishedByUserId == null || publishedByUserId.id() == null || publishedByUserId.id().isBlank()) {
            throw new IllegalArgumentException("Published-by user id is required");
        }
    }
}
