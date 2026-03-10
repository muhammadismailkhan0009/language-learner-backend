package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model;

import java.util.List;

public record VocabularyClozeSentence(
        VocabularyClozeSentenceId id,
        String clozeText,
        String hint,
        String answerText,
        List<String> answerWords,
        String answerTranslation
) {

    public record VocabularyClozeSentenceId(String id) {
    }
}
