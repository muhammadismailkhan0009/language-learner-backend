package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;

import java.util.List;
import java.util.Optional;

public interface GrammarRuleRepo {

    GrammarRule save(GrammarRule grammarRule);

    Optional<GrammarRule> findById(String grammarRuleId);

    List<GrammarRule> findAll();
}
