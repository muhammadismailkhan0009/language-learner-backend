package com.myriadcode.languagelearner.language_content.application.ports;

import java.util.List;

public record VocabularyClozeBatch(
        List<ClozeItem> clozeSentences
) {
    public record ClozeItem(
            String vocabSource,
            String clozeText,
            String hint,
            String filledSentence,
            List<String> answerWords,
            String filleSentenceTranslation
    ) {
    }
}
