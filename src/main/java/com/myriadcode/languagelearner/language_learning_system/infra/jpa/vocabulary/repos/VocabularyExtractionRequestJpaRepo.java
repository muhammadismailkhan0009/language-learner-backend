package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyExtractionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VocabularyExtractionRequestJpaRepo
        extends JpaRepository<VocabularyExtractionRequestEntity, String> {
    Optional<VocabularyExtractionRequestEntity> findFirstByUserIdOrderByCreatedAtAsc(String userId);
    Optional<VocabularyExtractionRequestEntity> findByIdAndUserId(String id, String userId);
    boolean existsByUserId(String userId);
}
