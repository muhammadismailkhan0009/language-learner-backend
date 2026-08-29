package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeScenarioEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReadingPracticeScenarioJpaRepo extends JpaRepository<ReadingPracticeScenarioEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select scenario from ReadingPracticeSessionEntity session join session.scenarios scenario " +
            "where session.userId = :userId and scenario.id = :scenarioId")
    Optional<ReadingPracticeScenarioEntity> findOwnedForUpdate(@Param("scenarioId") String scenarioId,
                                                               @Param("userId") String userId);

    @Query("select scenario from ReadingPracticeSessionEntity session join session.scenarios scenario " +
            "where session.userId = :userId and scenario.id = :scenarioId")
    Optional<ReadingPracticeScenarioEntity> findOwned(@Param("scenarioId") String scenarioId,
                                                      @Param("userId") String userId);
}
