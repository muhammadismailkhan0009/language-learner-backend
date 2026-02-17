package com.myriadcode.languagelearner.language_learning_system.scenarios.application.services;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.CreateScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.EditScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.response.ScenarioResponse;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.mappers.ScenarioApiMapper;
import com.myriadcode.languagelearner.language_learning_system.scenarios.domain.repo.ScenarioRepo;
import com.myriadcode.languagelearner.language_learning_system.scenarios.domain.services.ScenarioDomainService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioOrchestrationService {

    private static final ScenarioApiMapper SCENARIO_API_MAPPER = ScenarioApiMapper.INSTANCE;
    private final ScenarioRepo scenarioRepo;

    public ScenarioResponse createScenario(String userId, CreateScenarioRequest request) {
        var toSave = ScenarioDomainService.create(
                new UserId(userId),
                request.nature(),
                request.targetLanguage(),
                SCENARIO_API_MAPPER.toCreateSentences(request.sentences())
        );
        return SCENARIO_API_MAPPER.toResponse(scenarioRepo.save(toSave));
    }

    public ScenarioResponse editScenario(String userId, String scenarioId, EditScenarioRequest request) {
        var existing = scenarioRepo.findByIdAndUserId(scenarioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found for this user"));
        var toSave = ScenarioDomainService.edit(
                existing,
                request.nature(),
                request.targetLanguage(),
                SCENARIO_API_MAPPER.toUpdateSentences(request.sentences())
        );
        return SCENARIO_API_MAPPER.toResponse(scenarioRepo.save(toSave));
    }

    public List<ScenarioResponse> fetchScenarios(String userId) {
        return scenarioRepo.findByUserId(userId).stream()
                .map(SCENARIO_API_MAPPER::toResponse)
                .toList();
    }

    public ScenarioResponse fetchScenario(String userId, String scenarioId) {
        var scenario = scenarioRepo.findByIdAndUserId(scenarioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found for this user"));
        return SCENARIO_API_MAPPER.toResponse(scenario);
    }
}
