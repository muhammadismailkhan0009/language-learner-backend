package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities.GrammarRuleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrammarRuleEntityJpaRepo extends JpaRepository<GrammarRuleEntity, String> {

    @Override
    @EntityGraph(attributePaths = {"explanationParagraphs", "grammarScenario"})
    Optional<GrammarRuleEntity> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"explanationParagraphs", "grammarScenario"})
    List<GrammarRuleEntity> findAll();
}
