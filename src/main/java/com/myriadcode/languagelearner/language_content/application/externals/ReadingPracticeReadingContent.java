package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record ReadingPracticeReadingContent(
        List<Paragraph> paragraphs
) {
    public record Paragraph(String text, List<String> sentences) {
    }
}
