package com.myriadcode.languagelearner.language_content.domain.model;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;

import java.util.List;

public record Vocabulary(
        VocabularyId id,
        VocabularyData vocabularyData,
        LangConfigsAdaptive langConfigsAdaptive
) {

    public record VocabularyData(
            String root,
            PartOfSpeech type,
            String translation,
            List<WordForm> forms
    ) {
    }

    public record VocabularyId(String id) {
    }

    public record WordForm(
            String form,
            String grammaticalRole,
            String note
    ) {
    }

    public enum PartOfSpeech {
        NOUN,
        VERB,
        ADJECTIVE,
        ADVERB,
        PRONOUN,
        PREPOSITION,
        CONJUNCTION,
        INTERJECTION,
        NUMERAL,
        PARTICLE,
        DETERMINER,
        OTHER
    }
}
