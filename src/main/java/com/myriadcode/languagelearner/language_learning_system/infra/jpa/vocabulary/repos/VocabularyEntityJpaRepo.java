package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VocabularyEntityJpaRepo extends JpaRepository<VocabularyEntity, String> {

    @EntityGraph(attributePaths = {"exampleSentences", "clozeSentence"})
    Optional<VocabularyEntity> findByIdAndUserId(String id, String userId);

    @Override
    @EntityGraph(attributePaths = {"exampleSentences", "clozeSentence"})
    Optional<VocabularyEntity> findById(String id);

    @EntityGraph(attributePaths = {"exampleSentences", "clozeSentence"})
    List<VocabularyEntity> findAllByUserId(String userId);

    @EntityGraph(attributePaths = {"exampleSentences", "clozeSentence"})
    List<VocabularyEntity> findAllByIdIn(List<String> ids);
}
