package com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.myriadcode.languagelearner.language_learning_system.domain.scenarios.model.Scenario;
import com.myriadcode.languagelearner.language_learning_system.domain.scenarios.model.ScenarioSentence;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios.entities.ScenarioEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios.entities.ScenarioSentenceEntity;

@Mapper
public interface ScenarioJpaMapper {

    ScenarioJpaMapper INSTANCE = Mappers.getMapper(ScenarioJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "sentences", source = "sentences")
    ScenarioEntity toEntity(Scenario scenario);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "scenario", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    ScenarioSentenceEntity toSentenceEntity(ScenarioSentence sentence);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "sentences", source = "sentences")
    Scenario toDomain(ScenarioEntity entity);

    @Mapping(target = "id.id", source = "id")
    ScenarioSentence toSentenceDomain(ScenarioSentenceEntity sentenceEntity);
}
