package com.myriadcode.languagelearner.behavior.writing_feedback;

import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingFeedbackIssueSelector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WritingFeedbackIssueSelectorTest {

    private final WritingFeedbackIssueSelector selector = new WritingFeedbackIssueSelector();

    @Test
    void returnsAtMostThreeIssues() {
        var result = new WritingGrammarIssueDetectionResult(List.of(
                issue("a", 5, true, 1),
                issue("b", 4, true, 1),
                issue("c", 3, true, 1),
                issue("d", 2, true, 1)
        ));

        assertEquals(3, selector.selectTopIssues(result).size());
    }

    @Test
    void prioritizesTopCandidateThenPriorityThenOccurrenceCount() {
        var result = new WritingGrammarIssueDetectionResult(List.of(
                issue("low", 10, false, 10),
                issue("top-high", 7, true, 1),
                issue("top-repeat", 7, true, 3),
                issue("top-low", 1, true, 1)
        ));

        var selected = selector.selectTopIssues(result);

        assertEquals("top-repeat", selected.get(0).issueType());
        assertEquals("top-high", selected.get(1).issueType());
        assertEquals("top-low", selected.get(2).issueType());
    }

    @Test
    void handlesEmptyIssueList() {
        assertTrue(selector.selectTopIssues(new WritingGrammarIssueDetectionResult(List.of())).isEmpty());
    }

    private WritingGrammarIssueDetectionResult.Issue issue(String type, int priority, boolean topCandidate, int occurrenceCount) {
        return new WritingGrammarIssueDetectionResult.Issue(
                "rule",
                type,
                priority,
                "learner",
                "corrected",
                "explain",
                topCandidate,
                occurrenceCount
        );
    }
}
