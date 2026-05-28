package com.myriadcode.languagelearner.language_content.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.WritingSubmissionFeedbackLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingSubmissionFeedbackResult;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.springframework.stereotype.Service;

@Service
public class WritingSubmissionFeedbackLlmAdapter implements WritingSubmissionFeedbackLlmApi {

    private final LLMPort llmPort;

    public WritingSubmissionFeedbackLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public String generateFeedback(String englishParagraph,
                                   String referenceGermanParagraph,
                                   String submittedGermanParagraph) {
        return generateFeedback(englishParagraph, referenceGermanParagraph, submittedGermanParagraph, java.util.List.of()).feedback();
    }

    @Override
    public WritingSubmissionFeedbackResult generateFeedback(String englishParagraph,
                                                            String referenceGermanParagraph,
                                                            String submittedGermanParagraph,
                                                            java.util.List<GrammarRuleCatalogItem> grammarCatalog) {
        var result = (grammarCatalog == null || grammarCatalog.isEmpty())
                ? llmPort.generateWritingSubmissionFeedback(englishParagraph, referenceGermanParagraph, submittedGermanParagraph)
                : llmPort.generateWritingSubmissionFeedback(
                        englishParagraph,
                        referenceGermanParagraph,
                        submittedGermanParagraph,
                        grammarCatalog
                );
        if (result == null || result.feedback() == null || result.feedback().isBlank()) {
            return new WritingSubmissionFeedbackResult(
                    "Submission checked. Keep writing and focus on clear German sentence structure.",
                    java.util.List.of()
            );
        }
        return new WritingSubmissionFeedbackResult(
                result.feedback().trim(),
                result.grammarIssues() == null ? java.util.List.of() : result.grammarIssues().stream()
                        .map(issue -> new com.myriadcode.languagelearner.language_content.application.externals.GrammarFeedbackIssueResult(
                                issue.issueText(),
                                issue.message(),
                                issue.suggestion(),
                                issue.ruleIdentifier(),
                                issue.fallbackExplanation()
                        ))
                        .toList()
        );
    }
}
