package com.myriadcode.languagelearner.language_content;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.domain.repo.LanguageContentRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.ChunkEntityJpaRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.SentenceEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
public class LLMContentStoreAndFetchTest {

    @Autowired
    private LanguageContentRepo languageContentRepo;

    //    to clean up the data

    @Autowired
    private ChunkEntityJpaRepo chunkEntityJpaRepo;
    @Autowired
    private SentenceEntityJpaRepo sentenceEntityJpaRepo;


    LangConfigsAdaptive languageConfigs = new LangConfigsAdaptive(

            GermanAdaptive.GrammarRuleEnum.BASIC_PREPOSITIONS,
            GermanAdaptive.CommunicativeFunctionEnum.ASK_AND_ANSWER_SIMPLE_QUESTIONS,
            GermanAdaptive.ScenarioEnum.DIRECTIONS_AND_LOCATIONS,
            new LangConfigsAdaptive.GenerationQuantity(8)
    );

    @AfterEach
    public void tearDown() {
        chunkEntityJpaRepo.deleteAll();
        sentenceEntityJpaRepo.deleteAll();
    }


    @Test
    public void storeAndFetchChunks() {
        var chunks = List.of(
                new Chunk(new Chunk.ChunkId(UUID.randomUUID().toString()),
                        new Chunk.ChunkData("chunk1", "tra1", "note1"),
                        languageConfigs),
                new Chunk(new Chunk.ChunkId(UUID.randomUUID().toString()),
                        new Chunk.ChunkData("chunk2", "tra2", "note2"),
                        languageConfigs)
        );

        languageContentRepo.saveChunks(chunks);

        var previousChunks = languageContentRepo.getPreviousChunks();
        assertThat(previousChunks.size()).isEqualTo(chunks.size());

    }

    @Test
    public void storeAndFetchSentences() {
        var sentences = List.of(
                new Sentence(new Sentence.SentenceId(UUID.randomUUID().toString()),
                        new Sentence.SentenceData("chunk1", "tra1")
                        , languageConfigs),
                new Sentence(new Sentence.SentenceId(UUID.randomUUID().toString()),
                        new Sentence.SentenceData("chunk2", "tra2"),
                        languageConfigs)
        );

        languageContentRepo.saveSentences(sentences);

        var previousChunks = languageContentRepo.getPreviousSentences();
        assertThat(previousChunks.size()).isEqualTo(sentences.size());

    }
}
