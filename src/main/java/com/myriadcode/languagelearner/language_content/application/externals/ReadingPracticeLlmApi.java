package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.util.List;

public interface ReadingPracticeLlmApi {

    String selectTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                        List<String> previousTopics,
                                        LanguageLevel difficultyLevel);

    ReadingPracticeReadingContent generateReadingContent(List<ReadingPracticeVocabularySeed> vocabulary,
                                                         List<String> previousScenarioLabels,
                                                         LanguageLevel difficultyLevel,
                                                         List<String> grammarRuleTitles,
                                                         int scenarioCount);

    List<String> identifyUsedVocabulary(List<ReadingPracticeVocabularySeed> vocabulary,
                                        String readingText);
}
