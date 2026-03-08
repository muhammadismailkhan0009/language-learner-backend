package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public interface WritingPracticeLlmApi {

    String selectTopicForWriting(List<WritingPracticeVocabularySeed> vocabulary,
                                 List<String> previousTopics,
                                 String difficultyLevel);

    WritingPracticeBilingualContent generateBilingualContent(String topic,
                                                             List<WritingPracticeVocabularySeed> vocabulary,
                                                             String difficultyLevel);

    List<String> identifyUsedVocabulary(List<WritingPracticeVocabularySeed> vocabulary,
                                        String englishParagraph,
                                        String germanParagraph);

    List<WritingPracticeSentencePairSeed> splitIntoSentencePairs(String englishParagraph,
                                                                 String germanParagraph);
}
