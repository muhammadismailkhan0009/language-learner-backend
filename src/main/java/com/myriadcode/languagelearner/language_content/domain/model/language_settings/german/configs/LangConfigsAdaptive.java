package com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;

public record LangConfigsAdaptive(
        GermanAdaptive.GrammarRuleEnum rule,
        GermanAdaptive.CommunicativeFunctionEnum function,
        GermanAdaptive.ScenarioEnum scenario,
        GenerationQuantity quantity //FIXME: we must extract it separately in future.
) {
    public record GenerationQuantity(
            int sentenceCount
    ) {
    }
}
