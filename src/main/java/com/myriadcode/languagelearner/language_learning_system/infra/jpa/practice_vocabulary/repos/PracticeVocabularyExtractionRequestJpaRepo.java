package com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.entities.PracticeVocabularyExtractionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

@Deprecated(forRemoval = true)
public interface PracticeVocabularyExtractionRequestJpaRepo
        extends JpaRepository<PracticeVocabularyExtractionRequestEntity, String> {
}
