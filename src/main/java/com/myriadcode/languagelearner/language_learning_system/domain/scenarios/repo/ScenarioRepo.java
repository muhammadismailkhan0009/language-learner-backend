package com.myriadcode.languagelearner.language_learning_system.domain.scenarios.repo;

import java.util.List;
import java.util.Optional;

import com.myriadcode.languagelearner.language_learning_system.domain.scenarios.model.Scenario;

public interface ScenarioRepo {

    Scenario save(Scenario scenario);

    Optional<Scenario> findByIdAndUserId(String scenarioId, String userId);

    List<Scenario> findByUserId(String userId);
}
