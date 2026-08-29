package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyExtractionCandidateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VocabularyExtractionCandidateJpaRepo
        extends JpaRepository<VocabularyExtractionCandidateEntity, String> {
    List<VocabularyExtractionCandidateEntity> findByUserIdOrderByCreatedAtAsc(String userId);
    List<VocabularyExtractionCandidateEntity> findByUserIdAndCreatedVocabularyIdIsNullOrderByCreatedAtAsc(
            String userId);
    boolean existsByUserIdAndCreatedVocabularyIdIsNull(String userId);
}
