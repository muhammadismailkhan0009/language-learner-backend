package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities.StudySentencePoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudySentencePoolJpaRepo extends JpaRepository<StudySentencePoolEntity, String> {
    List<StudySentencePoolEntity> findAllByVocabularyIdOrderByCreatedAtAsc(String vocabularyId);
}
