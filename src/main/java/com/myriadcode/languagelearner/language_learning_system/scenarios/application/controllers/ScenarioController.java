package com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.CreateScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.EditScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.response.ScenarioResponse;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.services.ScenarioOrchestrationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/scenarios")
public class ScenarioController {

    private final ScenarioOrchestrationService scenarioOrchestrationService;

    public ScenarioController(ScenarioOrchestrationService scenarioOrchestrationService) {
        this.scenarioOrchestrationService = scenarioOrchestrationService;
    }

    @PostMapping("v1")
    public ResponseEntity<ApiResponse<ScenarioResponse>> createScenario(
            @RequestParam String userId,
            @RequestBody CreateScenarioRequest request
    ) {
        var response = scenarioOrchestrationService.createScenario(userId, request);
        return ResponseEntity.status(201).body(new ApiResponse<>(response));
    }

    @PutMapping("{scenarioId}/v1")
    public ResponseEntity<ApiResponse<ScenarioResponse>> editScenario(
            @RequestParam String userId,
            @PathVariable String scenarioId,
            @RequestBody EditScenarioRequest request
    ) {
        var response = scenarioOrchestrationService.editScenario(userId, scenarioId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<ScenarioResponse>>> fetchScenarios(
            @RequestParam String userId
    ) {
        var response = scenarioOrchestrationService.fetchScenarios(userId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("{scenarioId}/v1")
    public ResponseEntity<ApiResponse<ScenarioResponse>> fetchScenario(
            @RequestParam String userId,
            @PathVariable String scenarioId
    ) {
        var response = scenarioOrchestrationService.fetchScenario(userId, scenarioId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
