package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingGrammarIssueAnalyticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WritingGrammarIssueAnalyticsJpaRepo extends JpaRepository<WritingGrammarIssueAnalyticsEntity, String> {
    List<WritingGrammarIssueAnalyticsEntity> findAllBySessionIdAndUserIdOrderByPriorityDescCreatedAtAsc(String sessionId, String userId);

    void deleteBySessionIdAndUserId(String sessionId, String userId);
}
