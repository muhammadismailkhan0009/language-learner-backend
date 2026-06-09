package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingGrammarIssueAnalytics;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos.WritingPracticeSessionJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "writing.feedback.structured-enabled=true")
@ActiveProfiles("test")
@Import({TestDbConfigs.class, WritingPracticeSessionFlowTests.WritingPracticeTestDoubles.class})
class WritingFeedbackReEvaluationFlowContextTest {

    @Autowired
    private WritingPracticeService writingPracticeService;

    @Autowired
    private WritingPracticeSessionJpaRepo writingPracticeSessionJpaRepo;

    @Autowired
    private WritingPracticeRepo writingPracticeRepo;

    @AfterEach
    void tearDown() {
        writingPracticeSessionJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("reEvaluateFeedback: upgrades legacy feedback and replaces grammar analytics")
    void reEvaluateFeedbackUpgradesLegacyFeedbackAndReplacesAnalytics() {
        writingPracticeService.createSession("user-1");
        var sessionId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();
        var submittedAt = Instant.parse("2026-01-01T00:00:00Z");
        writingPracticeRepo.updateSubmission(
                sessionId,
                "user-1",
                "Vielleicht ich gehe.",
                submittedAt,
                "Legacy feedback only.",
                Instant.parse("2026-01-01T00:01:00Z")
        );
        writingPracticeRepo.saveGrammarIssueAnalytics(List.of(new WritingGrammarIssueAnalytics(
                new WritingGrammarIssueAnalytics.WritingGrammarIssueAnalyticsId("old-analytics"),
                new com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession.WritingPracticeSessionId(sessionId),
                new com.myriadcode.languagelearner.common.ids.UserId("user-1"),
                "old-rule",
                "old_issue",
                1,
                "old",
                "old corrected",
                "old explanation",
                1,
                submittedAt
        )));

        var response = writingPracticeService.reEvaluateFeedback("user-1", sessionId);

        assertThat(response.submittedAnswer()).isEqualTo("Vielleicht ich gehe.");
        assertThat(response.submittedAt()).isEqualTo(submittedAt);
        assertThat(response.feedbackText()).contains("Overall: Meaning: partial.");
        assertThat(response.structuredFeedback()).isNotNull();
        assertThat(response.structuredFeedback().topFixes()).hasSize(1);
        assertThat(response.feedbackGeneratedAt()).isAfter(submittedAt);

        var analytics = writingPracticeRepo.findGrammarIssueAnalytics(sessionId, "user-1");
        assertThat(analytics).hasSize(1);
        assertThat(analytics.getFirst().id().id()).isNotEqualTo("old-analytics");
        assertThat(analytics.getFirst().issueType()).isEqualTo("word_order");
    }

    @Test
    @DisplayName("reEvaluateFeedback: rejects sessions without submitted answers")
    void reEvaluateFeedbackRejectsUnsubmittedSession() {
        writingPracticeService.createSession("user-1");
        var sessionId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();

        assertThatThrownBy(() -> writingPracticeService.reEvaluateFeedback("user-1", sessionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Writing session must have a submitted answer before re-evaluation");
    }
}
