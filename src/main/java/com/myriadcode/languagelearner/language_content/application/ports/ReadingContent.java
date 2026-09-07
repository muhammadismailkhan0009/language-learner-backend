package com.myriadcode.languagelearner.language_content.application.ports;

import java.util.List;

public record ReadingContent(List<Scenario> scenarios) {
    public record Scenario(String scenarioLabel, List<Paragraph> paragraphs, List<String> usedVocabulary) {}
    public record Paragraph(String text, List<String> sentences) {
    }
}
