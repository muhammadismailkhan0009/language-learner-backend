package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public interface GrammarRuleCurationLlmApi {

    List<GrammarRuleDraftProposal> proposeRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules);

    GrammarRuleDraftDetails generateRuleDetails(String identifier, String name, String level, String targetLanguage);
}
