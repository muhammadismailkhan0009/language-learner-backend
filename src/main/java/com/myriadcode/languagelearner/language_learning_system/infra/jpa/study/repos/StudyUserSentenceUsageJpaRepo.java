package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities.StudyUserSentenceUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyUserSentenceUsageJpaRepo extends JpaRepository<StudyUserSentenceUsageEntity, String> {
    Optional<StudyUserSentenceUsageEntity> findByUserIdAndSentenceId(String userId, String sentenceId);
    List<StudyUserSentenceUsageEntity> findAllByUserId(String userId);
}
