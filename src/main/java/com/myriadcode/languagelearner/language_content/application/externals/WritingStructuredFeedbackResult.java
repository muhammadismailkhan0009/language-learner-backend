package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingStructuredFeedbackResult(
        String overall,
        String correctedParagraph,
        List<TopFix> topFixes,
        VocabularySummary vocabulary,
        List<SentenceCorrection> sentenceCorrections,
        List<MicroPracticeItem> microPractice,
        String nextFocus
) {
    public record TopFix(String title, String learnerText, String correctedText, String explanation) {
    }

    public record VocabularySummary(List<String> good, List<String> needsPractice) {
    }

    public record SentenceCorrection(String learnerSentence, String correctedSentence, String explanation) {
    }

    public record MicroPracticeItem(String prompt, String expectedAnswer) {
    }
}
