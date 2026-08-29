package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import java.util.List;

public record VocabularyDetailSelection(List<Detail> details) {
    public record Detail(String candidateId, String surface, String translation, String entryKind,
                         String notes, List<ExampleSentence> exampleSentences) {}
    public record ExampleSentence(String sentence, String translation) {}
}
