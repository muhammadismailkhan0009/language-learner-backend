package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingGrammarIssueAnalytics;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingStructuredFeedback;

import java.util.List;
import java.util.Optional;

public interface WritingPracticeRepo {

    WritingPracticeSession save(WritingPracticeSession session);

    Optional<WritingPracticeSession> findByIdAndUserId(String sessionId, String userId);

    List<WritingPracticeSession> findAllByUserId(String userId);

    List<String> findRecentTopicsByUserId(String userId, int limit);

    WritingPracticeSession updateSubmission(String sessionId,
                                            String userId,
                                            String submittedAnswer,
                                            java.time.Instant submittedAt,
                                            String feedbackText,
                                            WritingStructuredFeedback structuredFeedback,
                                            java.time.Instant feedbackGeneratedAt);

    default WritingPracticeSession updateSubmission(String sessionId,
                                                    String userId,
                                                    String submittedAnswer,
                                                    java.time.Instant submittedAt,
                                                    String feedbackText,
                                                    java.time.Instant feedbackGeneratedAt) {
        return updateSubmission(sessionId, userId, submittedAnswer, submittedAt, feedbackText, null, feedbackGeneratedAt);
    }

    default void saveGrammarIssueAnalytics(List<WritingGrammarIssueAnalytics> analytics) {
    }

    default List<WritingGrammarIssueAnalytics> findGrammarIssueAnalytics(String sessionId, String userId) {
        return List.of();
    }

    default void deleteGrammarIssueAnalytics(String sessionId, String userId) {
    }

    void deleteByIdAndUserId(String sessionId, String userId);

    void detachFlashcard(String userId, String sessionId, String flashcardId);
}
