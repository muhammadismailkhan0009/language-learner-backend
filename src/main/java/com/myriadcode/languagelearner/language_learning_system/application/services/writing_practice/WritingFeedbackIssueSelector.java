package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class WritingFeedbackIssueSelector {

    public List<WritingGrammarIssueDetectionResult.Issue> selectTopIssues(WritingGrammarIssueDetectionResult result) {
        if (result == null || result.issues() == null || result.issues().isEmpty()) {
            return List.of();
        }
        return result.issues().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(WritingGrammarIssueDetectionResult.Issue::topCandidate).reversed()
                        .thenComparing(WritingGrammarIssueDetectionResult.Issue::priority, Comparator.reverseOrder())
                        .thenComparing(WritingGrammarIssueDetectionResult.Issue::occurrenceCount, Comparator.reverseOrder()))
                .limit(3)
                .toList();
    }
}
