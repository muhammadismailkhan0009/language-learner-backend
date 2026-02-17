package com.myriadcode.languagelearner.language_learning_system.domain.scenarios.domain.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.scenarios.domain.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepo {

    Scenario save(Scenario scenario);

    Optional<Scenario> findByIdAndUserId(String scenarioId, String userId);

    List<Scenario> findByUserId(String userId);
}
