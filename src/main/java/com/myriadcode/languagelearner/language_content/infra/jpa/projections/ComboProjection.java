package com.myriadcode.languagelearner.language_content.infra.jpa.projections;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;

public interface ComboProjection {
    GermanAdaptive.ScenarioEnum getScenario();

    GermanAdaptive.GrammarRuleEnum getGrammarRule();

    GermanAdaptive.CommunicativeFunctionEnum getCommunicationFunction();
}
