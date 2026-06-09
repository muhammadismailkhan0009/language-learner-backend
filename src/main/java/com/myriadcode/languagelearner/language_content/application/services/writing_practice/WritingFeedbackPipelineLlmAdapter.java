package com.myriadcode.languagelearner.language_content.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackPipelineLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackVocabularyItem;
import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingMeaningAnalysisResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingStructuredFeedbackResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingVocabularyEvaluationResult;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WritingFeedbackPipelineLlmAdapter implements WritingFeedbackPipelineLlmApi {

    private final LLMPort llmPort;

    public WritingFeedbackPipelineLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public WritingMeaningAnalysisResult analyzeMeaning(String learnerLevel,
                                                       String englishPrompt,
                                                       String referenceGermanParagraph,
                                                       String learnerGermanAnswer) {
        return llmPort.analyzeWritingMeaning(learnerLevel, englishPrompt, referenceGermanParagraph, learnerGermanAnswer);
    }

    @Override
    public WritingVocabularyEvaluationResult evaluateVocabulary(String learnerLevel,
                                                               String englishPrompt,
                                                               String referenceGermanParagraph,
                                                               String learnerGermanAnswer,
                                                               List<WritingFeedbackVocabularyItem> selectedVocabulary,
                                                               WritingMeaningAnalysisResult meaningAnalysis) {
        return llmPort.evaluateWritingVocabulary(
                learnerLevel,
                englishPrompt,
                referenceGermanParagraph,
                learnerGermanAnswer,
                selectedVocabulary == null ? List.of() : selectedVocabulary,
                meaningAnalysis
        );
    }

    @Override
    public WritingGrammarIssueDetectionResult detectGrammarIssues(String learnerLevel,
                                                                  String englishPrompt,
                                                                  String referenceGermanParagraph,
                                                                  String learnerGermanAnswer,
                                                                  List<GrammarRuleCatalogItem> grammarCatalog,
                                                                  WritingMeaningAnalysisResult meaningAnalysis,
                                                                  WritingVocabularyEvaluationResult vocabularyEvaluation) {
        return llmPort.detectWritingGrammarIssues(
                learnerLevel,
                englishPrompt,
                referenceGermanParagraph,
                learnerGermanAnswer,
                grammarCatalog == null ? List.of() : grammarCatalog,
                meaningAnalysis,
                vocabularyEvaluation
        );
    }

    @Override
    public WritingStructuredFeedbackResult composeFeedback(String learnerLevel,
                                                          String englishPrompt,
                                                          String referenceGermanParagraph,
                                                          String learnerGermanAnswer,
                                                          WritingMeaningAnalysisResult meaningAnalysis,
                                                          WritingVocabularyEvaluationResult vocabularyEvaluation,
                                                          WritingGrammarIssueDetectionResult grammarIssues,
                                                          List<WritingGrammarIssueDetectionResult.Issue> selectedTopIssues) {
        return llmPort.composeWritingFeedback(
                learnerLevel,
                englishPrompt,
                referenceGermanParagraph,
                learnerGermanAnswer,
                meaningAnalysis,
                vocabularyEvaluation,
                grammarIssues,
                selectedTopIssues == null ? List.of() : selectedTopIssues
        );
    }
}
