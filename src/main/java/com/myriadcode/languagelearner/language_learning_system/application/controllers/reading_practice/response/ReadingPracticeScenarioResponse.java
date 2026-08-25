package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response;

import java.util.List;

public record ReadingPracticeScenarioResponse(String scenarioId, String topic, String readingText,
                                              List<ReadingPracticeParagraphResponse> readingParagraphs,
                                              List<ReadingVocabularyFlashCardView> vocabFlashcards) {}
