package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.repos;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities.GrammarGenerationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrammarGenerationRequestJpaRepo extends JpaRepository<GrammarGenerationRequestEntity, String> {
    Optional<GrammarGenerationRequestEntity> findFirstByUserIdAndTypeOrderByCreatedAtAsc(
            String userId, GrammarGenerationRequest.Type type);
    boolean existsByUserIdAndType(String userId, GrammarGenerationRequest.Type type);
}
