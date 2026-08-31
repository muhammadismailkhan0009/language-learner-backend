package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services.GrammarRuleVisibilityPolicy;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;

@Service
public class WritingGenerationContextService {

    private final UserDifficultyLevelApi userDifficultyLevelApi;
    private final GrammarRuleRepo grammarRuleRepo;

    public WritingGenerationContextService(UserDifficultyLevelApi userDifficultyLevelApi,
                                           GrammarRuleRepo grammarRuleRepo) {
        this.userDifficultyLevelApi = userDifficultyLevelApi;
        this.grammarRuleRepo = grammarRuleRepo;
    }

    public WritingGenerationContext build(String userId) {
        var unifiedLevel = userDifficultyLevelApi.getDifficultyLevel(userId);
        var writingLevel = userDifficultyLevelApi.getWritingDifficultyLevel(userId);
        var titles = grammarRuleRepo.findAll().stream()
                .filter(rule -> GrammarRuleVisibilityPolicy.isVisibleTo(rule, unifiedLevel))
                .map(rule -> rule.name() == null ? "" : rule.name().trim())
                .filter(title -> !title.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return new WritingGenerationContext(writingLevel, java.util.List.copyOf(titles));
    }
}
