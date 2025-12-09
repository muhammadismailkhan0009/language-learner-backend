package com.myriadcode.languagelearner.language_content.infra.jpa.mappers;

import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.infra.jpa.entities.ChunkEntity;
import com.myriadcode.languagelearner.language_content.infra.jpa.entities.SentenceEntity;
import com.myriadcode.languagelearner.language_content.infra.jpa.projections.ComboProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LanguageContentMapper {

    LanguageContentMapper INSTANCE = Mappers.getMapper(LanguageContentMapper.class);

    @Mappings({
            @Mapping(target = "chunk", source = "data.chunk"),
            @Mapping(target = "translation", source = "data.translation"),
            @Mapping(target = "id", source = "id.id"),

    })
    ChunkEntity toChunkEntity(Chunk chunk);

    @Mappings({
            @Mapping(source = "chunk", target = "data.chunk"),
            @Mapping(source = "translation", target = "data.translation"),
            @Mapping(source = "id", target = "id.id"),

    })
    Chunk toChunkDomain(ChunkEntity chunk);

    @Mappings({
            @Mapping(source = "chunk", target = "chunk"),
            @Mapping(source = "translation", target = "translation")
    })
    Chunk.ChunkData toChunkData(ChunkEntity chunk);


    @Mappings({
            @Mapping(target = "sentence", source = "data.sentence"),
            @Mapping(target = "translation", source = "data.translation"),
            @Mapping(target = "id", source = "id.id"),

            @Mapping(target = "scenario", source = "langConfigsAdaptive.scenario"),
            @Mapping(target = "communicationFunction", source = "langConfigsAdaptive.function"),
            @Mapping(target = "grammarRule", source = "langConfigsAdaptive.rule")


    })
    SentenceEntity toSentenceEntity(Sentence sentence);

    @Mappings({
            @Mapping(source = "sentence", target = "data.sentence"),
            @Mapping(source = "translation", target = "data.translation"),
            @Mapping(source = "id", target = "id.id"),

            @Mapping(target = "langConfigsAdaptive", source = ".", qualifiedByName = "mapConfigs")
    })
    Sentence toSentenceDomain(SentenceEntity sentence);

    @Named("mapConfigs")
    default LangConfigsAdaptive mapConfigs(SentenceEntity sentence) {
        return new LangConfigsAdaptive(
                sentence.getGrammarRule(),
                sentence.getCommunicationFunction(),
                sentence.getScenario(),
                new LangConfigsAdaptive.GenerationQuantity(8)
        );
    }

    @Mappings({
            @Mapping(source = "sentence", target = "sentence"),
            @Mapping(source = "translation", target = "translation")
    })
    Sentence.SentenceData toSentenceData(SentenceEntity sentence);


    @Mappings({
            @Mapping(source = "comboProjection.scenario", target = "scenario"),
            @Mapping(source = "comboProjection.communicationFunction", target = "function"),
            @Mapping(source = "comboProjection.grammarRule", target = "rule"),
            @Mapping(target = "quantity", source = "sentenceCount")

    })
    LangConfigsAdaptive mapBlitzLesson(ComboProjection comboProjection, int sentenceCount);

    default LangConfigsAdaptive.GenerationQuantity map(int value) {
        return new LangConfigsAdaptive.GenerationQuantity(value);
    }

}
