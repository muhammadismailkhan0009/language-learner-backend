package com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.entities.PublicVocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicVocabularyEntityJpaRepo extends JpaRepository<PublicVocabularyEntity, String> {

    Optional<PublicVocabularyEntity> findBySourceVocabularyId(String sourceVocabularyId);

    @Override
    Optional<PublicVocabularyEntity> findById(String id);

    List<PublicVocabularyEntity> findAllByStatusOrderByPublishedAtDesc(String status);
}
