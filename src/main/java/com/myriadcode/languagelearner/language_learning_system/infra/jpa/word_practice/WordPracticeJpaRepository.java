package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.Set;

interface WordPracticeJpaRepository extends JpaRepository<WordPracticeEntity, String> {
    @Query("select count(distinct p.vocabularyId) from WordPracticeEntity p where p.userId = :userId")
    long countDistinctVocabularyByUserId(@Param("userId") String userId);
    @Query("select distinct p.vocabularyId from WordPracticeEntity p where p.userId = :userId")
    Set<String> findActiveVocabularyIds(@Param("userId") String userId);
    List<WordPracticeEntity> findAllByUserIdOrderByCreatedAtAscIdAsc(String userId);
    Optional<WordPracticeEntity> findByIdAndUserId(String id, String userId);
    long deleteByIdAndUserId(String id, String userId);
}
