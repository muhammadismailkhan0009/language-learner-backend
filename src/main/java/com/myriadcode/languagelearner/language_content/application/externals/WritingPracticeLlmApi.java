package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.util.List;

public interface WritingPracticeLlmApi {

    String selectTopicForWriting(List<WritingPracticeVocabularySeed> vocabulary,
                                 List<String> previousTopics,
                                 LanguageLevel difficultyLevel);

    WritingPracticeBilingualContent generateBilingualContent(String topic,
                                                             List<WritingPracticeVocabularySeed> vocabulary,
                                                             LanguageLevel difficultyLevel,
                                                             List<String> grammarRuleTitles);

    List<String> identifyUsedVocabulary(List<WritingPracticeVocabularySeed> vocabulary,
                                        String englishParagraph,
                                        String germanParagraph);

    List<WritingPracticeSentencePairSeed> splitIntoSentencePairs(String englishParagraph,
                                                                 String germanParagraph);
}
