package com.myriadcode.languagelearner.language_content.infra.jpa;

import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.UserStatsForContent;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.ContentGenerationQuantity;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.domain.repo.LanguageContentRepo;
import com.myriadcode.languagelearner.language_content.domain.repo.UserStatsRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.mappers.LanguageContentMapper;
import com.myriadcode.languagelearner.language_content.infra.jpa.mappers.UserStatsMapper;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.ChunkEntityJpaRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.SentenceEntityJpaRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.UserStatsJpaRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaRepoImpl implements LanguageContentRepo, UserStatsRepo {

    @Autowired
    private ChunkEntityJpaRepo chunkEntityJpaRepo;

    @Autowired
    private SentenceEntityJpaRepo sentenceEntityJpaRepo;

    @Autowired
    private UserStatsJpaRepo userStatsJpaRepo;

    @Override
    public void saveChunks(List<Chunk> chunks) {

        var entities = chunks.parallelStream().map(LanguageContentMapper.INSTANCE::toChunkEntity).toList();
        chunkEntityJpaRepo.saveAll(entities);
    }

    @Override
    public void saveSentences(List<Sentence> sentences) {
        var entities = sentences.parallelStream().map(LanguageContentMapper.INSTANCE::toSentenceEntity).toList();
        sentenceEntityJpaRepo.saveAll(entities);
    }

    @Override
    public List<Chunk.ChunkData> getPreviousChunks() {
        var previousChunks = chunkEntityJpaRepo.findAll();
        return previousChunks.parallelStream().map(
                LanguageContentMapper.INSTANCE::toChunkData
        ).toList();
    }

    @Override
    public List<Sentence.SentenceData> getPreviousSentences() {
        var sentence = sentenceEntityJpaRepo.findAll();
        return sentence.parallelStream().map(LanguageContentMapper.INSTANCE::toSentenceData).toList();
    }

    @Override
    public Chunk.ChunkData getChunk(String chunkId) {
        System.out.println(chunkId);
        var entity = chunkEntityJpaRepo.findById(chunkId);
        if (entity.isEmpty()) {
            throw new EntityNotFoundException(chunkId);
        }
        return LanguageContentMapper.INSTANCE.toChunkData(entity.get());
    }

    @Override
    public Sentence.SentenceData getSentence(String sentenceId) {
        var entity = sentenceEntityJpaRepo.findById(sentenceId);
        if (entity.isEmpty()) {
            throw new EntityNotFoundException(sentenceId);
        }
        return LanguageContentMapper.INSTANCE.toSentenceData(entity.get());
    }

    @Override
    public List<LangConfigsAdaptive> getBlitzLessonsForWhichSentencesAreGenerated() {
        var combos = sentenceEntityJpaRepo.findDistinctCombos();
        return combos.parallelStream().map(
                combo -> LanguageContentMapper.INSTANCE.mapBlitzLesson(combo, ContentGenerationQuantity.SENTENCES.getNumber())).toList();
    }

    @Override
    public List<Sentence> getSentencesForLangConfig(LangConfigsAdaptive langConfigsAdaptive) {
        var entities = sentenceEntityJpaRepo.findAllByScenarioAndGrammarRuleAndCommunicationFunction(
                langConfigsAdaptive.scenario(),
                langConfigsAdaptive.rule(),
                langConfigsAdaptive.function()
        );

        return entities.parallelStream().map(LanguageContentMapper.INSTANCE::toSentenceDomain).toList();
    }

    @Override
    public List<Sentence.SentenceData> getSentencesForScenario(GermanAdaptive.ScenarioEnum scenario) {
        var entities = sentenceEntityJpaRepo.findAllByScenario(scenario);
        return entities.parallelStream().map(LanguageContentMapper.INSTANCE::toSentenceData).toList();
    }

    @Override
    public List<UserStatsForContent> getUserStatsForContent(List<String> userIds) {
        var entities = userStatsJpaRepo.findAllByUserIdIn(userIds);
        return entities.parallelStream().map(UserStatsMapper.INSTANCE::toDomain).toList();
    }

    @Override
    public UserStatsForContent save(UserStatsForContent userStatsForContent) {
        var entity = UserStatsMapper.INSTANCE.toEntity(userStatsForContent);
        entity.setId(UUID.randomUUID().toString());
        var savedEntity = userStatsJpaRepo.save(entity);

        return UserStatsMapper.INSTANCE.toDomain(savedEntity);
    }
}
