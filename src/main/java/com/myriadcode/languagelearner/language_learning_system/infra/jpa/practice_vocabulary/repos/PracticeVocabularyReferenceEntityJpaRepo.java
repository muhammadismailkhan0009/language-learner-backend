package com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.entities.PracticeVocabularyReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PracticeVocabularyReferenceEntityJpaRepo extends JpaRepository<PracticeVocabularyReferenceEntity, String> {

    Optional<PracticeVocabularyReferenceEntity> findByUserIdAndVocabularyId(String userId, String vocabularyId);

    List<PracticeVocabularyReferenceEntity> findAllByUserIdOrderByCreatedAtAsc(String userId);
}
