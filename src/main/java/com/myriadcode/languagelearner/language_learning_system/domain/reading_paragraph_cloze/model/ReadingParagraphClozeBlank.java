package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model;

import java.util.List;

public record ReadingParagraphClozeBlank(ReadingParagraphClozeBlankId id, int blankIndex,
                                         String blankToken, String exactAnswer, String answerExplanation,
                                         PracticeKind practiceKind, String vocabularyId,
                                         List<String> grammarRuleIds) {
    public ReadingParagraphClozeBlank {
        grammarRuleIds = grammarRuleIds == null ? List.of() : List.copyOf(grammarRuleIds);
    }
    public enum PracticeKind { VOCABULARY_FORM, GRAMMAR, VOCABULARY_AND_GRAMMAR }
    public record ReadingParagraphClozeBlankId(String id) {}
}
