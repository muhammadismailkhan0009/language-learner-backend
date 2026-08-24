package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response;

import java.time.Instant;
import java.util.List;

public record ReadingParagraphClozeSessionResponse(
        String sessionId,
        String learnerLevel,
        Instant createdAt,
        List<Paragraph> paragraphs
) {
    public record Paragraph(String paragraphId, int paragraphIndex, String scenarioLabel,
                            String clozeParagraph, List<Blank> blanks) {}
    public record Blank(String blankId, int blankIndex, String blankToken, String exactAnswer,
                        String answerExplanation, String practiceKind, VocabularyDetails vocabularyDetails,
                        List<GrammarRuleDetails> grammarRuleDetails) {}
    public record VocabularyDetails(String id, String surface, String translation, String entryKind,
                                    String notes, List<ExampleSentence> exampleSentences) {}
    public record ExampleSentence(String sentence, String translation) {}
    public record GrammarRuleDetails(String id, String identifier, String name, String level,
                                     List<String> explanationParagraphs, List<ExampleSentence> explanationExamples) {}
}
