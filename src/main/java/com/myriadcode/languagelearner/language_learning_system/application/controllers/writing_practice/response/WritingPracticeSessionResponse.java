package com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response;

import java.time.Instant;
import java.util.List;

public record WritingPracticeSessionResponse(
        String sessionId,
        String topic,
        String englishParagraph,
        String germanParagraph,
        String submittedAnswer,
        Instant submittedAt,
        String feedbackText,
        WritingStructuredFeedbackResponse structuredFeedback,
        Instant feedbackGeneratedAt,
        List<WritingSentencePairResponse> sentencePairs,
        List<WritingVocabularyFlashCardView> vocabFlashcards,
        Instant createdAt
) {
    public WritingPracticeSessionResponse(
            String sessionId,
            String topic,
            String englishParagraph,
            String germanParagraph,
            String submittedAnswer,
            Instant submittedAt,
            String feedbackText,
            Instant feedbackGeneratedAt,
            List<WritingSentencePairResponse> sentencePairs,
            List<WritingVocabularyFlashCardView> vocabFlashcards,
            Instant createdAt
    ) {
        this(sessionId, topic, englishParagraph, germanParagraph, submittedAnswer, submittedAt, feedbackText,
                null, feedbackGeneratedAt, sentencePairs, vocabFlashcards, createdAt);
    }

    public record WritingStructuredFeedbackResponse(
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
}
