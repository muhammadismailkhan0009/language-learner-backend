package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingVocabularyEvaluationResult(List<Item> items) {
    public record Item(
            String vocabularyId,
            String germanTarget,
            String status,
            String memorySignal,
            String learnerText,
            String explanation
    ) {
    }
}
