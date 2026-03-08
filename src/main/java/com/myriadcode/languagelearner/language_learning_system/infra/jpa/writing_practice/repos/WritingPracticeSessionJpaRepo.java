package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeSessionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WritingPracticeSessionJpaRepo extends JpaRepository<WritingPracticeSessionEntity, String> {

    @EntityGraph(attributePaths = {"vocabularyUsages", "sentencePairs"})
    Optional<WritingPracticeSessionEntity> findByIdAndUserId(String id, String userId);

    List<WritingPracticeSessionEntity> findAllByUserIdOrderByCreatedAtDesc(String userId);

    List<WritingPracticeSessionEntity> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    void deleteByIdAndUserId(String id, String userId);
}
