package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record ClozeParagraphGeneration(List<Paragraph> paragraphs) {
    public record Paragraph(String scenarioLabel, String clozeParagraph, List<Blank> blanks) {}
    public record Blank(String blankToken, String exactAnswer, String answerExplanation,
                        String practiceKind, String vocabularyId, List<String> grammarRuleIds) {}
}
