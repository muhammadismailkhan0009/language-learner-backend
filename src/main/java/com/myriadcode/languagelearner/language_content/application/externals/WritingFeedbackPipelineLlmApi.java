package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public interface WritingFeedbackPipelineLlmApi {

    WritingMeaningAnalysisResult analyzeMeaning(String learnerLevel,
                                                String englishPrompt,
                                                String referenceGermanParagraph,
                                                String learnerGermanAnswer);

    WritingVocabularyEvaluationResult evaluateVocabulary(String learnerLevel,
                                                         String englishPrompt,
                                                         String referenceGermanParagraph,
                                                         String learnerGermanAnswer,
                                                         List<WritingFeedbackVocabularyItem> selectedVocabulary,
                                                         WritingMeaningAnalysisResult meaningAnalysis);

    WritingGrammarIssueDetectionResult detectGrammarIssues(String learnerLevel,
                                                           String englishPrompt,
                                                           String referenceGermanParagraph,
                                                           String learnerGermanAnswer,
                                                           List<GrammarRuleCatalogItem> grammarCatalog,
                                                           WritingMeaningAnalysisResult meaningAnalysis,
                                                           WritingVocabularyEvaluationResult vocabularyEvaluation);

    WritingStructuredFeedbackResult composeFeedback(String learnerLevel,
                                                    String englishPrompt,
                                                    String referenceGermanParagraph,
                                                    String learnerGermanAnswer,
                                                    WritingMeaningAnalysisResult meaningAnalysis,
                                                    WritingVocabularyEvaluationResult vocabularyEvaluation,
                                                    WritingGrammarIssueDetectionResult grammarIssues,
                                                    List<WritingGrammarIssueDetectionResult.Issue> selectedTopIssues);
}
