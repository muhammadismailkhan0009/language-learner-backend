package com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios.repos;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.scenarios.entities.ScenarioEntity;

import java.util.List;
import java.util.Optional;

public interface ScenarioEntityJpaRepo extends JpaRepository<ScenarioEntity, String> {

    @EntityGraph(attributePaths = "sentences")
    Optional<ScenarioEntity> findByIdAndUserId(String id, String userId);

    @EntityGraph(attributePaths = "sentences")
    List<ScenarioEntity> findAllByUserId(String userId);
}
