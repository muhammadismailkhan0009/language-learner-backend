package com.myriadcode.languagelearner.language_content.application.ports;

import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingContent;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;

import java.util.List;

public interface LLMPort {

    List<Chunk.ChunkData> generateChunks(LangConfigsAdaptive langconfigs, List<Sentence.SentenceData> sentences,
                                         List<Chunk.ChunkData> previousChunks);

    List<Vocabulary.VocabularyData> generateVocabulary(LangConfigsAdaptive langConfigs,
                                                       List<Chunk.ChunkData> chunks,
                                                       List<Sentence.SentenceData> sentences);

    List<Sentence.SentenceData> generateSentences(LangConfigsAdaptive languageConfigs,
                                                  List<Sentence.SentenceData> previousSentences);

    ReadingTopicSelection selectReadingTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                                              String difficultyLevel);

    ReadingContent generateReadingContent(String topic,
                                          List<ReadingPracticeVocabularySeed> vocabulary,
                                          String difficultyLevel);

    WritingTopicSelection selectWritingTopicForTextGeneration(List<WritingPracticeVocabularySeed> vocabulary,
                                                              List<String> previousTopics,
                                                              String difficultyLevel);

    WritingBilingualContent generateWritingBilingualContent(String topic,
                                                            List<WritingPracticeVocabularySeed> vocabulary,
                                                            String difficultyLevel);

    WritingUsedVocabularySelection identifyUsedWritingVocabulary(List<WritingPracticeVocabularySeed> vocabulary,
                                                                 String englishParagraph,
                                                                 String germanParagraph);

    WritingSentencePairSplit splitWritingContentIntoSentencePairs(String englishParagraph,
                                                                  String germanParagraph);

}
