package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record ReadingPracticeReadingContent(List<Scenario> scenarios) {
    public record Scenario(String scenarioLabel, List<Paragraph> paragraphs, List<UsedVocabulary> usedVocabulary) {}
    public record UsedVocabulary(String vocabularyId, String surface) {}
    public record Paragraph(String text, List<String> sentences) {
    }
}
