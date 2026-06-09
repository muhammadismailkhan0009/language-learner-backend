package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingVocabularyEvaluationResult(List<Item> items) {
    public record Item(
            String vocabularyId,
            String germanTarget,
            VocabularyStatus status,
            VocabularyMemorySignal memorySignal,
            String learnerText,
            String explanation
    ) {
    }

    public enum VocabularyStatus {
        correct,
        partially_correct,
        missing,
        wrong,
        used_english,
        no_evidence
    }

    public enum VocabularyMemorySignal {
        production_good,
        production_hard,
        production_again,
        no_update
    }
}
