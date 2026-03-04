package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;

import java.util.List;
import java.util.Optional;

public interface ReadingPracticeRepo {

    ReadingPracticeSession save(ReadingPracticeSession session);

    Optional<ReadingPracticeSession> findByIdAndUserId(String sessionId, String userId);

    List<ReadingPracticeSession> findAllByUserId(String userId);

    void deleteByIdAndUserId(String sessionId, String userId);
}
