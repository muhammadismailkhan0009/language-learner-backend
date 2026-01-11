package com.myriadcode.languagelearner.language_content.domain.repo;

import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface LanguageContentRepo {

    void saveChunks(List<Chunk> chunk);

    void saveSentences(List<Sentence> sentences);

    List<Chunk.ChunkData> getPreviousChunks();

    List<Sentence.SentenceData> getPreviousSentences();

    Chunk.ChunkData getChunk(String chunkId);

    Sentence.SentenceData getSentence(String sentenceId);

    List<LangConfigsAdaptive> getBlitzLessonsForWhichSentencesAreGenerated();

    List<Sentence> getSentencesForLangConfig(@NotNull LangConfigsAdaptive langConfigsAdaptive);

    List<Sentence.SentenceData> getSentencesForScenario(@NotNull GermanAdaptive.ScenarioEnum scenario);

    List<Sentence> getAllSentences();

}
