package com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyExtractionRequest;

import java.util.Optional;

public interface PracticeVocabularyExtractionRequestRepo {
    PracticeVocabularyExtractionRequest save(PracticeVocabularyExtractionRequest request);
    Optional<PracticeVocabularyExtractionRequest> findByUserId(String userId);
    void deleteByUserId(String userId);
}
