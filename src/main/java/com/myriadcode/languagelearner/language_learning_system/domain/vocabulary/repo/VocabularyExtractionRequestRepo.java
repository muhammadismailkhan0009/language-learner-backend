package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionRequest;

import java.util.Optional;

public interface VocabularyExtractionRequestRepo {
    VocabularyExtractionRequest save(VocabularyExtractionRequest request);
    Optional<VocabularyExtractionRequest> findOldestByUserId(String userId);
    Optional<VocabularyExtractionRequest> findByIdAndUserId(String requestId, String userId);
    boolean existsByUserId(String userId);
    void delete(VocabularyExtractionRequest.VocabularyExtractionRequestId id);
}
