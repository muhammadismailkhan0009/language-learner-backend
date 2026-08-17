package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleResponse;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.grammar_rules.GrammarRuleApiMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services.GrammarRuleVisibilityPolicy;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileGrammarRuleQueryService {

    private static final GrammarRuleApiMapper GRAMMAR_RULE_API_MAPPER = GrammarRuleApiMapper.INSTANCE;
    private final GrammarRuleRepo grammarRuleRepo;
    private final UserDifficultyLevelApi userDifficultyLevelApi;

    public ProfileGrammarRuleQueryService(GrammarRuleRepo grammarRuleRepo,
                                          UserDifficultyLevelApi userDifficultyLevelApi) {
        this.grammarRuleRepo = grammarRuleRepo;
        this.userDifficultyLevelApi = userDifficultyLevelApi;
    }

    public List<GrammarRuleResponse> fetchGrammarRules(String userId) {
        var profileLevel = userDifficultyLevelApi.getDifficultyLevel(userId);
        return grammarRuleRepo.findAll().stream()
                .filter(rule -> GrammarRuleVisibilityPolicy.isVisibleTo(rule, profileLevel))
                .map(GRAMMAR_RULE_API_MAPPER::toResponse)
                .toList();
    }
}
