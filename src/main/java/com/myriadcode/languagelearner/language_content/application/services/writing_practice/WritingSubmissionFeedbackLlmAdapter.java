package com.myriadcode.languagelearner.language_content.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.WritingSubmissionFeedbackLlmApi;
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
        var result = llmPort.generateWritingSubmissionFeedback(
                englishParagraph,
                referenceGermanParagraph,
                submittedGermanParagraph
        );
        if (result == null || result.feedback() == null || result.feedback().isBlank()) {
            return "Submission checked. Keep writing and focus on clear German sentence structure.";
        }
        return result.feedback().trim();
    }
}
