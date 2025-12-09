package com.myriadcode.languagelearner.language_content.application.ports;

import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;

import java.util.List;

public interface LLMPort {

    List<Chunk.ChunkData> generateChunks(LangConfigsAdaptive langconfigs, List<Sentence.SentenceData> sentences,
                                         List<Chunk.ChunkData> previousChunks);

    List<Vocabulary.VocabularyData> generateVocabulary(LangConfigsAdaptive langConfigs,
                                                       List<Chunk.ChunkData> chunks,
                                                       List<Sentence.SentenceData> sentences);

    List<Sentence.SentenceData> generateSentences(LangConfigsAdaptive languageConfigs);

}
