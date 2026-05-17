package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities.StudyAnswerLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyAnswerLogJpaRepo extends JpaRepository<StudyAnswerLogEntity, String> {
}
