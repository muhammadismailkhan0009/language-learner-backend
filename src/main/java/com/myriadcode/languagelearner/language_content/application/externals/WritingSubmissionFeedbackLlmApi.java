package com.myriadcode.languagelearner.language_content.application.externals;

public interface WritingSubmissionFeedbackLlmApi {

    String generateFeedback(String englishParagraph,
                            String referenceGermanParagraph,
                            String submittedGermanParagraph);

    default WritingSubmissionFeedbackResult generateFeedback(String englishParagraph,
                                                             String referenceGermanParagraph,
                                                             String submittedGermanParagraph,
                                                             java.util.List<GrammarRuleCatalogItem> grammarCatalog) {
        return new WritingSubmissionFeedbackResult(
                generateFeedback(englishParagraph, referenceGermanParagraph, submittedGermanParagraph),
                java.util.List.of()
        );
    }
}
