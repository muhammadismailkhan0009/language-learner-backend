package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.common.enums.ClozePracticeKind;
import java.util.List;

public record ClozeParagraphGeneration(List<Paragraph> paragraphs) {
    public record Paragraph(String scenarioLabel, String clozeParagraph, List<Blank> blanks) {}
    public record Blank(String blankToken, String exactAnswer, String answerExplanation,
                        ClozePracticeKind practiceKind, String vocabularyId, List<String> grammarRuleIds) {}
}
