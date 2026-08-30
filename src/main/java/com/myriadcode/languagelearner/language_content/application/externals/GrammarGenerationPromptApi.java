package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;

import java.util.List;

public interface GrammarGenerationPromptApi {
    String ruleDraftPrompt(String level, String targetLanguage, int count,
                           List<GrammarRuleCatalogContext> existingRules);

    String ruleDetailsPrompt(String level, String targetLanguage,
                             List<GrammarGenerationRequest.RuleSeed> rules);

    String levelReassignmentPrompt(List<GrammarLevelReassignmentInput> grammarRules);
}
