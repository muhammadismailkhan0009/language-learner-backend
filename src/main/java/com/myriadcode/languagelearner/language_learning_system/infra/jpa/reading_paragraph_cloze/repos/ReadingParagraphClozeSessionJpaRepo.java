package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.ReadingParagraphClozeSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReadingParagraphClozeSessionJpaRepo extends JpaRepository<ReadingParagraphClozeSessionEntity, String> {

    Optional<ReadingParagraphClozeSessionEntity> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    Optional<ReadingParagraphClozeSessionEntity> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query(value = """
            delete from reading_paragraph_cloze_paragraph p
            where p.session_id = :sessionId
              and not exists (
                select 1
                from reading_paragraph_cloze_card c
                where c.session_id = :sessionId
                  and c.paragraph_id = p.id
              )
            """, nativeQuery = true)
    int deleteOrphanParagraphs(@Param("sessionId") String sessionId);
}
