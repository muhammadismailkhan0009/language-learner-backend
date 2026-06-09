package com.myriadcode.languagelearner.language_content.application.ports;

import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingContent;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackVocabularyItem;
import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingMeaningAnalysisResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingStructuredFeedbackResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingVocabularyEvaluationResult;
import com.myriadcode.languagelearner.language_content.application.ports.GrammarRuleDraftDetailsPort;
import com.myriadcode.languagelearner.language_content.application.ports.GrammarRuleDraftProposalPort;

import java.util.List;

public interface LLMPort {

    List<Chunk.ChunkData> generateChunks(LangConfigsAdaptive langconfigs, List<Sentence.SentenceData> sentences,
                                         List<Chunk.ChunkData> previousChunks);

    List<Vocabulary.VocabularyData> generateVocabulary(LangConfigsAdaptive langConfigs,
                                                       List<Chunk.ChunkData> chunks,
                                                       List<Sentence.SentenceData> sentences);

    List<Sentence.SentenceData> generateSentences(LangConfigsAdaptive languageConfigs,
                                                  List<Sentence.SentenceData> previousSentences);

    ReadingTopicSelection selectReadingTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                                              List<String> previousTopics,
                                                              String difficultyLevel);

    ReadingContent generateReadingContent(String topic,
                                          List<ReadingPracticeVocabularySeed> vocabulary,
                                          String difficultyLevel);
    ReadingParagraphClozeGeneration generateReadingParagraphCloze(String topic,
                                                                  List<ReadingPracticeVocabularySeed> vocabulary,
                                                                  String difficultyLevel);

    ReadingUsedVocabularySelection identifyUsedReadingVocabulary(List<ReadingPracticeVocabularySeed> vocabulary,
                                                                 String readingText);

    WritingTopicSelection selectWritingTopicForTextGeneration(List<WritingPracticeVocabularySeed> vocabulary,
                                                              List<String> previousTopics,
                                                              String difficultyLevel);

    WritingBilingualContent generateWritingBilingualContent(String topic,
                                                            List<WritingPracticeVocabularySeed> vocabulary,
                                                            String difficultyLevel);

    WritingUsedVocabularySelection identifyUsedWritingVocabulary(List<WritingPracticeVocabularySeed> vocabulary,
                                                                 String englishParagraph,
                                                                 String germanParagraph);

    WritingSentencePairSplit splitWritingContentIntoSentencePairs(String englishParagraph,
                                                                  String germanParagraph);

    WritingSubmissionFeedback generateWritingSubmissionFeedback(String englishParagraph,
                                                               String referenceGermanParagraph,
                                                               String submittedGermanParagraph);

    default WritingSubmissionFeedback generateWritingSubmissionFeedback(String englishParagraph,
                                                                       String referenceGermanParagraph,
                                                                       String submittedGermanParagraph,
                                                                       List<GrammarRuleCatalogItem> grammarCatalog) {
        return generateWritingSubmissionFeedback(englishParagraph, referenceGermanParagraph, submittedGermanParagraph);
    }

    WritingMeaningAnalysisResult analyzeWritingMeaning(String learnerLevel,
                                                       String englishPrompt,
                                                       String referenceGermanParagraph,
                                                       String learnerGermanAnswer);

    WritingVocabularyEvaluationResult evaluateWritingVocabulary(String learnerLevel,
                                                                String englishPrompt,
                                                                String referenceGermanParagraph,
                                                                String learnerGermanAnswer,
                                                                List<WritingFeedbackVocabularyItem> selectedVocabulary,
                                                                WritingMeaningAnalysisResult meaningAnalysis);

    WritingGrammarIssueDetectionResult detectWritingGrammarIssues(String learnerLevel,
                                                                  String englishPrompt,
                                                                  String referenceGermanParagraph,
                                                                  String learnerGermanAnswer,
                                                                  List<GrammarRuleCatalogItem> grammarCatalog,
                                                                  WritingMeaningAnalysisResult meaningAnalysis,
                                                                  WritingVocabularyEvaluationResult vocabularyEvaluation);

    WritingStructuredFeedbackResult composeWritingFeedback(String learnerLevel,
                                                           String englishPrompt,
                                                           String referenceGermanParagraph,
                                                           String learnerGermanAnswer,
                                                           WritingMeaningAnalysisResult meaningAnalysis,
                                                           WritingVocabularyEvaluationResult vocabularyEvaluation,
                                                           WritingGrammarIssueDetectionResult grammarIssues,
                                                           List<WritingGrammarIssueDetectionResult.Issue> selectedTopIssues);

    VocabularyClozeBatch generateVocabularyClozeSentences(String topic,
                                                          List<VocabularyClozeGenerationSeed> vocabulary);

    StudyAnswerEvaluation evaluateStudyAnswer(String sentenceWithBlank,
                                              String expectedAnswer,
                                              String answerTranslation,
                                              String hint,
                                              String userAnswer);

    default StudyAnswerEvaluation evaluateStudyAnswer(String sentenceWithBlank,
                                                      String expectedAnswer,
                                                      String answerTranslation,
                                                      String hint,
                                                      String userAnswer,
                                                      List<GrammarRuleCatalogItem> grammarCatalog) {
        return evaluateStudyAnswer(sentenceWithBlank, expectedAnswer, answerTranslation, hint, userAnswer);
    }

    List<GrammarRuleDraftProposalPort> proposeGrammarRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules);

    GrammarRuleDraftDetailsPort generateGrammarRuleDetails(String identifier, String name, String level, String targetLanguage);

}
