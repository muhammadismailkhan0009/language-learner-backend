package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model;

import com.myriadcode.languagelearner.common.enums.ClozePracticeKind;
import java.util.List;

public record ReadingParagraphClozeBlank(ReadingParagraphClozeBlankId id, int blankIndex,
                                         String blankToken, String exactAnswer, String answerExplanation,
                                         ClozePracticeKind practiceKind, String vocabularyId,
                                         List<String> grammarRuleIds) {
    public ReadingParagraphClozeBlank {
        grammarRuleIds = grammarRuleIds == null ? List.of() : List.copyOf(grammarRuleIds);
    }
    public record ReadingParagraphClozeBlankId(String id) {}
}
