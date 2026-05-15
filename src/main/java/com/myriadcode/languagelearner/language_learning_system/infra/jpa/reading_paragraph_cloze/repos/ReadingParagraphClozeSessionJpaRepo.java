package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.ReadingParagraphClozeSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingParagraphClozeSessionJpaRepo extends JpaRepository<ReadingParagraphClozeSessionEntity, String> {

    Optional<ReadingParagraphClozeSessionEntity> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    Optional<ReadingParagraphClozeSessionEntity> findByIdAndUserId(String id, String userId);
}
