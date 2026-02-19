package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.mappers.GrammarRuleJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.repos.GrammarRuleEntityJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class GrammarRuleJpaRepoImpl implements GrammarRuleRepo {

    private static final GrammarRuleJpaMapper GRAMMAR_RULE_JPA_MAPPER = GrammarRuleJpaMapper.INSTANCE;
    private final GrammarRuleEntityJpaRepo grammarRuleEntityJpaRepo;

    public GrammarRuleJpaRepoImpl(GrammarRuleEntityJpaRepo grammarRuleEntityJpaRepo) {
        this.grammarRuleEntityJpaRepo = grammarRuleEntityJpaRepo;
    }

    @Override
    @Transactional
    public GrammarRule save(GrammarRule grammarRule) {
        var entity = GRAMMAR_RULE_JPA_MAPPER.toEntity(grammarRule);
        for (int i = 0; i < entity.getExplanationParagraphs().size(); i++) {
            entity.getExplanationParagraphs().get(i).setDisplayOrder(i);
        }
        for (int i = 0; i < entity.getGrammarScenario().getSentences().size(); i++) {
            entity.getGrammarScenario().getSentences().get(i).setDisplayOrder(i);
        }
        var saved = grammarRuleEntityJpaRepo.save(entity);
        return GRAMMAR_RULE_JPA_MAPPER.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GrammarRule> findById(String grammarRuleId) {
        return grammarRuleEntityJpaRepo.findById(grammarRuleId)
                .map(GRAMMAR_RULE_JPA_MAPPER::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrammarRule> findAll() {
        return grammarRuleEntityJpaRepo.findAll().stream()
                .map(GRAMMAR_RULE_JPA_MAPPER::toDomain)
                .toList();
    }
}
