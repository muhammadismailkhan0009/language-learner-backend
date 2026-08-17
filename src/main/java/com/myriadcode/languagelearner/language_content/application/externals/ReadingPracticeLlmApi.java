package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.util.List;

public interface ReadingPracticeLlmApi {

    String selectTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                        List<String> previousTopics,
                                        LanguageLevel difficultyLevel);

    ReadingPracticeReadingContent generateReadingContent(String topic,
                                                         List<ReadingPracticeVocabularySeed> vocabulary,
                                                         LanguageLevel difficultyLevel,
                                                         List<String> grammarRuleTitles);

    ReadingParagraphClozeGeneration generateReadingParagraphCloze(String topic,
                                                                  List<ReadingPracticeVocabularySeed> vocabulary,
                                                                  String difficultyLevel);

    List<String> identifyUsedVocabulary(List<ReadingPracticeVocabularySeed> vocabulary,
                                        String readingText);
}
