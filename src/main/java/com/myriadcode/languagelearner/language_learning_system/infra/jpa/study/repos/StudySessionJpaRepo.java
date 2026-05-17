package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities.StudySessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudySessionJpaRepo extends JpaRepository<StudySessionEntity, String> {
    Optional<StudySessionEntity> findFirstByUserIdOrderByCreatedAtDesc(String userId);
}
