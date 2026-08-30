package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.language_content.application.externals.*;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrammarGenerationPromptAdapter implements GrammarGenerationPromptApi {
    public String ruleDraftPrompt(String level, String targetLanguage, int count,
                                  List<GrammarRuleCatalogContext> existingRules) {
        return PromptsGenerator.grammarRuleDrafts(level, targetLanguage, count, existingRules);
    }

    public String ruleDetailsPrompt(String level, String targetLanguage,
                                    List<GrammarGenerationRequest.RuleSeed> rules) {
        return rules.stream().map(rule -> PromptsGenerator.grammarRuleDetails(
                rule.identifier(), rule.name(), level, targetLanguage)).reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
    }

    public String levelReassignmentPrompt(List<GrammarLevelReassignmentInput> grammarRules) {
        return PromptsGenerator.grammarLevelReassignment(grammarRules);
    }
}
