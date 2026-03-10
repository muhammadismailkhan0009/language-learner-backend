package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record VocabularyClozeSentenceResult(
        String vocabSource,
        String clozeText,
        String hint,
        String answerText,
        List<String> answerWords,
        String answerTranslation
) {
}
