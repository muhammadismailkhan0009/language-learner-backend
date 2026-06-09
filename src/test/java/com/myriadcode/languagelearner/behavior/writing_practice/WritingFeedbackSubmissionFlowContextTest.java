package com.myriadcode.languagelearner.behavior.writing_practice;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos.WritingPracticeSessionJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "writing.feedback.structured-enabled=true")
@ActiveProfiles("test")
@Import({TestDbConfigs.class, WritingPracticeSessionFlowTests.WritingPracticeTestDoubles.class})
class WritingFeedbackSubmissionFlowContextTest {

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
    @DisplayName("submitAnswer: structured pipeline persists feedback and grammar analytics in real context")
    void submitAnswerPersistsStructuredFeedbackAndGrammarAnalytics() {
        writingPracticeService.createSession("user-1");
        var sessionId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();

        writingPracticeService.submitAnswer("user-1", sessionId, "Vielleicht ich gehe.");

        var response = writingPracticeService.getSession("user-1", sessionId);
        assertThat(response.submittedAnswer()).isEqualTo("Vielleicht ich gehe.");
        assertThat(response.feedbackText()).contains("Overall: Meaning: partial.");
        assertThat(response.structuredFeedback()).isNotNull();
        assertThat(response.structuredFeedback().topFixes()).hasSize(1);
        assertThat(response.structuredFeedback().nextFocus()).contains("verb-second");

        var analytics = writingPracticeRepo.findGrammarIssueAnalytics(sessionId, "user-1");
        assertThat(analytics).hasSize(1);
        assertThat(analytics.getFirst().issueType()).isEqualTo("word_order");
        assertThat(analytics.getFirst().learnerText()).isEqualTo("Vielleicht ich gehe");
    }
}
