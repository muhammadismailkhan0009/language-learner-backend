package com.myriadcode.languagelearner.language_content.application.controllers.sentences.response;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;

import java.util.List;

public record SentenceDataResponse(GermanAdaptive.ScenarioEnum scenario, List<SentenceFunction> functions) {

    public record SentenceFunction(GermanAdaptive.CommunicativeFunctionEnum function, List<SentenceContent> sentence) {}
    public record SentenceContent(String sentence, String translation){}
}
