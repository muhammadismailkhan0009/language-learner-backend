package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingSubmissionFeedbackResult(
        String feedback,
        List<GrammarFeedbackIssueResult> grammarIssues
) {
}

