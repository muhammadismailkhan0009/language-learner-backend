package com.myriadcode.languagelearner.language_learning_system.application.externals;

import java.util.List;

public record PrivateVocabularyRecord(
        String id,
        String userId,
        String surface,
        String translation,
        String entryKind,
        List<ExampleSentenceRecord> exampleSentences
) {
    public record ExampleSentenceRecord(
            String id,
            String sentence,
            String translation
    ) {
    }
}
