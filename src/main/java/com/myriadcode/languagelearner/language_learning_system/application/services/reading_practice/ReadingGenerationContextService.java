package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingGrammarEligibilityPolicy;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import org.springframework.stereotype.Service;

@Service
public class ReadingGenerationContextService {

    private final UserDifficultyLevelApi userDifficultyLevelApi;
    private final GrammarRuleRepo grammarRuleRepo;
    private final ReadingGrammarEligibilityPolicy eligibilityPolicy = new ReadingGrammarEligibilityPolicy();

    public ReadingGenerationContextService(UserDifficultyLevelApi userDifficultyLevelApi,
                                           GrammarRuleRepo grammarRuleRepo) {
        this.userDifficultyLevelApi = userDifficultyLevelApi;
        this.grammarRuleRepo = grammarRuleRepo;
    }

    public ReadingGenerationContext build(String userId) {
        var learnerLevel = userDifficultyLevelApi.getDifficultyLevel(userId);
        var candidates = grammarRuleRepo.findAll().stream()
                .map(rule -> new ReadingGrammarEligibilityPolicy.Candidate(
                        rule.name(),
                        parseLevel(rule.level()),
                        rule.active()
                ))
                .toList();
        return new ReadingGenerationContext(learnerLevel, eligibilityPolicy.selectTitles(learnerLevel, candidates));
    }

    private LanguageLevel parseLevel(String rawLevel) {
        try {
            return LanguageLevel.from(rawLevel);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
