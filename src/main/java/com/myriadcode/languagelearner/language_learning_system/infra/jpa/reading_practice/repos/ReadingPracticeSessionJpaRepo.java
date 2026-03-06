package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeSessionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingPracticeSessionJpaRepo extends JpaRepository<ReadingPracticeSessionEntity, String> {

    @EntityGraph(attributePaths = {"vocabularyUsages", "paragraphs", "paragraphs.sentences"})
    Optional<ReadingPracticeSessionEntity> findByIdAndUserId(String id, String userId);

    List<ReadingPracticeSessionEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByIdAndUserId(String id, String userId);
}
