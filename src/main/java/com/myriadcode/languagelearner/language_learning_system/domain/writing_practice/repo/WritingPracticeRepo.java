package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;

import java.util.List;
import java.util.Optional;

public interface WritingPracticeRepo {

    WritingPracticeSession save(WritingPracticeSession session);

    Optional<WritingPracticeSession> findByIdAndUserId(String sessionId, String userId);

    List<WritingPracticeSession> findAllByUserId(String userId);

    List<String> findRecentTopicsByUserId(String userId, int limit);

    WritingPracticeSession updateSubmission(String sessionId, String userId, String submittedAnswer, java.time.Instant submittedAt);

    void deleteByIdAndUserId(String sessionId, String userId);

    void detachFlashcard(String userId, String sessionId, String flashcardId);
}
