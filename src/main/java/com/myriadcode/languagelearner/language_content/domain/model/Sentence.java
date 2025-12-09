package com.myriadcode.languagelearner.language_content.domain.model;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import jakarta.validation.constraints.NotNull;

public record Sentence(SentenceId id, SentenceData data, @NotNull LangConfigsAdaptive langConfigsAdaptive) {
    public record SentenceId(String id) {
    }

    public record SentenceData(String sentence, String translation) {
    }
}
