package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionCandidate;

import java.util.List;

public interface VocabularyExtractionCandidateRepo {
    List<VocabularyExtractionCandidate> saveAll(List<VocabularyExtractionCandidate> candidates);
    List<VocabularyExtractionCandidate> findByUserId(String userId);
    List<VocabularyExtractionCandidate> findPendingByUserId(String userId);
    boolean existsPendingByUserId(String userId);
}
