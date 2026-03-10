package com.myriadcode.languagelearner.language_learning_system.application.externals;

import java.util.List;
import java.time.Instant;

public record PrivateVocabularyRecord(
        String id,
        String userId,
        String surface,
        String translation,
        String entryKind,
        String notes,
        List<ExampleSentenceRecord> exampleSentences,
        ClozeSentenceRecord clozeSentence,
        Instant createdAt
) {
    public record ExampleSentenceRecord(
            String id,
            String sentence,
            String translation
    ) {
    }

    public record ClozeSentenceRecord(
            String id,
            String clozeText,
            String hint,
            String answerText,
            List<String> answerWords,
            String answerTranslation
    ) {
    }
}
