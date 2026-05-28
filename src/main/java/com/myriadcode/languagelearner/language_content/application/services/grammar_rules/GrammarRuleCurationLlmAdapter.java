package com.myriadcode.languagelearner.language_content.application.services.grammar_rules;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCurationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftProposal;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrammarRuleCurationLlmAdapter implements GrammarRuleCurationLlmApi {

    private final LLMPort llmPort;

    public GrammarRuleCurationLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public List<GrammarRuleDraftProposal> proposeRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
        var result = llmPort.proposeGrammarRules(level, targetLanguage, count, existingRules);
        if (result == null) {
            return List.of();
        }
        return result.stream()
                .map(item -> new GrammarRuleDraftProposal(item.identifier(), item.name(), item.level(), item.targetLanguage()))
                .toList();
    }

    @Override
    public GrammarRuleDraftDetails generateRuleDetails(String identifier, String name, String level, String targetLanguage) {
        var details = llmPort.generateGrammarRuleDetails(identifier, name, level, targetLanguage);
        if (details == null) {
            return new GrammarRuleDraftDetails(identifier, name, level, targetLanguage, List.of(), List.of());
        }
        return new GrammarRuleDraftDetails(
                details.identifier(),
                details.name(),
                details.level(),
                details.targetLanguage(),
                details.explanationParagraphs() == null ? List.of() : details.explanationParagraphs(),
                details.explanationExamples() == null ? List.of() : details.explanationExamples().stream()
                        .map(example -> new GrammarRuleDraftDetails.GrammarRuleExample(
                                example.sentence(),
                                example.translation(),
                                example.note()
                        ))
                        .toList()
        );
    }
}
