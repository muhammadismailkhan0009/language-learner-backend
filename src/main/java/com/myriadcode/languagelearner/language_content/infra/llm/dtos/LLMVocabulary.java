package com.myriadcode.languagelearner.language_content.infra.llm.dtos;

import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;

public record LLMVocabulary(String root,
                            Vocabulary.PartOfSpeech type,
                            String translation,
                            LLMWordForm form) {
    public record LLMWordForm(String wordForm,
                              String grammaticalRole,
                              String note) {
    }
}