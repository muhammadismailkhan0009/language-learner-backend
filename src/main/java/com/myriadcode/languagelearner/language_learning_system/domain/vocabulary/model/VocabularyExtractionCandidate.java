package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model;

import com.myriadcode.languagelearner.common.ids.UserId;
import java.time.Instant;

public record VocabularyExtractionCandidate(
        VocabularyExtractionCandidateId id,
        UserId userId,
        String surface,
        String normalizedSurface,
        String createdVocabularyId,
        Instant createdAt
) {
    public VocabularyExtractionCandidate markCreated(String vocabularyId) {
        return new VocabularyExtractionCandidate(
                id, userId, surface, normalizedSurface, vocabularyId, createdAt);
    }

    public record VocabularyExtractionCandidateId(String id) {}
}
