package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities.StudySessionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionItemJpaRepo extends JpaRepository<StudySessionItemEntity, String> {
    List<StudySessionItemEntity> findAllBySessionIdOrderByQueueRankSnapshotAsc(String sessionId);
    Optional<StudySessionItemEntity> findByIdAndSessionId(String id, String sessionId);
}
