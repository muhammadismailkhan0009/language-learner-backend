package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public interface ReadingPracticeLlmApi {

    String selectTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary, String difficultyLevel);

    ReadingPracticeReadingContent generateReadingContent(String topic,
                                                         List<ReadingPracticeVocabularySeed> vocabulary,
                                                         String difficultyLevel);
}
