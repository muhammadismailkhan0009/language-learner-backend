package com.myriadcode.languagelearner.language_content.application.externals;

public interface WritingSubmissionFeedbackLlmApi {

    String generateFeedback(String englishParagraph,
                            String referenceGermanParagraph,
                            String submittedGermanParagraph);
}
