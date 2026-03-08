package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public interface ReadingPracticeLlmApi {

    String selectTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                        List<String> previousTopics,
                                        String difficultyLevel);

    ReadingPracticeReadingContent generateReadingContent(String topic,
                                                         List<ReadingPracticeVocabularySeed> vocabulary,
                                                         String difficultyLevel);

    List<String> identifyUsedVocabulary(List<ReadingPracticeVocabularySeed> vocabulary,
                                        String readingText);
}
