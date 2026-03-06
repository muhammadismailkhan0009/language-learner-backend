package com.myriadcode.languagelearner.language_content.application.ports;

import java.util.List;

public record ReadingParagraphSentenceSplit(
        List<ParagraphSentences> paragraphs
) {
    public record ParagraphSentences(int paragraphIndex, List<String> sentences) {
    }
}
