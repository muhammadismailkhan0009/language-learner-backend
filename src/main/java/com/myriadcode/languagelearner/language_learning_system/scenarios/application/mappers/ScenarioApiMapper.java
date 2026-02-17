package com.myriadcode.languagelearner.language_learning_system.scenarios.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.CreateScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.EditScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.response.ScenarioResponse;
import com.myriadcode.languagelearner.language_learning_system.scenarios.domain.model.Scenario;
import com.myriadcode.languagelearner.language_learning_system.scenarios.domain.model.ScenarioSentence;

import java.util.List;

@Mapper
public interface ScenarioApiMapper {

    ScenarioApiMapper INSTANCE = Mappers.getMapper(ScenarioApiMapper.class);

    @Mapping(target = "id", ignore = true)
    ScenarioSentence toCreateSentence(CreateScenarioRequest.ScenarioSentenceRequest sentence);

    List<ScenarioSentence> toCreateSentences(
            List<CreateScenarioRequest.ScenarioSentenceRequest> sentences);

    @Mapping(target = "id.id", source = "id")
    ScenarioSentence toUpdateSentence(EditScenarioRequest.ScenarioSentenceUpdateRequest sentence);

    List<ScenarioSentence> toUpdateSentences(
            List<EditScenarioRequest.ScenarioSentenceUpdateRequest> sentences);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "sentences", source = "sentences")
    ScenarioResponse toResponse(Scenario scenario);

    @Mapping(target = "id", source = "id.id")
    ScenarioResponse.ScenarioSentenceResponse toSentenceResponse(ScenarioSentence sentence);
}
