package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeScenario;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReadingPracticeRepo {

    ReadingPracticeSession save(ReadingPracticeSession session);

    Optional<ReadingPracticeSession> findByIdAndUserId(String sessionId, String userId);

    Optional<ReadingPracticeScenario> findScenarioByIdAndUserIdForUpdate(String scenarioId, String userId);

    Optional<ReadingPracticeScenario> findScenarioByIdAndUserId(String scenarioId, String userId);

    ReadingPracticeScenario saveScenarioProgress(ReadingPracticeScenario scenario);

    List<ReadingPracticeSession> findAllByUserId(String userId);

    List<String> findRecentTopicsByUserId(String userId, int limit);

    default List<Set<String>> findRecentVocabularyUsageSessionSetsByUserId(String userId, int limit) {
        return List.of();
    }

    void deleteByIdAndUserId(String sessionId, String userId);

    void detachFlashcard(String userId, String sessionId, String flashcardId);
}
