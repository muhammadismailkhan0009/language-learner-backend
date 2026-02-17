package com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios;

import org.springframework.stereotype.Repository;

import com.myriadcode.languagelearner.language_learning_system.domain.scenarios.model.Scenario;
import com.myriadcode.languagelearner.language_learning_system.domain.scenarios.repo.ScenarioRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios.mappers.ScenarioJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios.repos.ScenarioEntityJpaRepo;

import java.util.List;
import java.util.Optional;

@Repository
public class ScenarioJpaRepoImpl implements ScenarioRepo {

    private static final ScenarioJpaMapper SCENARIO_JPA_MAPPER = ScenarioJpaMapper.INSTANCE;
    private final ScenarioEntityJpaRepo scenarioEntityJpaRepo;

    public ScenarioJpaRepoImpl(ScenarioEntityJpaRepo scenarioEntityJpaRepo) {
        this.scenarioEntityJpaRepo = scenarioEntityJpaRepo;
    }

    @Override
    public Scenario save(Scenario scenario) {
        var entity = SCENARIO_JPA_MAPPER.toEntity(scenario);
        for (int i = 0; i < entity.getSentences().size(); i++) {
            entity.getSentences().get(i).setDisplayOrder(i);
        }
        var saved = scenarioEntityJpaRepo.save(entity);
        return SCENARIO_JPA_MAPPER.toDomain(saved);
    }

    @Override
    public Optional<Scenario> findByIdAndUserId(String scenarioId, String userId) {
        return scenarioEntityJpaRepo.findByIdAndUserId(scenarioId, userId)
                .map(SCENARIO_JPA_MAPPER::toDomain);
    }

    @Override
    public List<Scenario> findByUserId(String userId) {
        return scenarioEntityJpaRepo.findAllByUserId(userId).stream()
                .map(SCENARIO_JPA_MAPPER::toDomain)
                .toList();
    }
}
