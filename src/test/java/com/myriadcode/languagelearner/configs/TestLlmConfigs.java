package com.myriadcode.languagelearner.configs;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackVocabularyItem;
import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingMeaningAnalysisResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingStructuredFeedbackResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingVocabularyEvaluationResult;
import com.myriadcode.languagelearner.language_content.application.ports.GrammarRuleDraftDetailsPort;
import com.myriadcode.languagelearner.language_content.application.ports.GrammarRuleDraftProposalPort;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingContent;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_content.application.ports.StudyAnswerEvaluation;
import com.myriadcode.languagelearner.language_content.application.ports.VocabularyClozeBatch;
import com.myriadcode.languagelearner.language_content.application.ports.WritingBilingualContent;
import com.myriadcode.languagelearner.language_content.application.ports.WritingSentencePairSplit;
import com.myriadcode.languagelearner.language_content.application.ports.WritingSubmissionFeedback;
import com.myriadcode.languagelearner.language_content.application.ports.WritingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.ports.WritingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("test")
public class TestLlmConfigs {

    @Bean
    @Primary
    LLMPort deterministicTestLlmPort() {
        return new LLMPort() {
            @Override
            public List<Chunk.ChunkData> generateChunks(LangConfigsAdaptive langconfigs, List<Sentence.SentenceData> sentences, List<Chunk.ChunkData> previousChunks) {
                return List.of();
            }

            @Override
            public List<Vocabulary.VocabularyData> generateVocabulary(LangConfigsAdaptive langConfigs, List<Chunk.ChunkData> chunks, List<Sentence.SentenceData> sentences) {
                return List.of();
            }

            @Override
            public List<Sentence.SentenceData> generateSentences(LangConfigsAdaptive languageConfigs, List<Sentence.SentenceData> previousSentences) {
                return List.of();
            }

            @Override
            public ReadingTopicSelection selectReadingTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary, List<String> previousTopics, String difficultyLevel) {
                return new ReadingTopicSelection("Test reading topic");
            }

            @Override
            public ReadingContent generateReadingContent(String topic, List<ReadingPracticeVocabularySeed> vocabulary, String difficultyLevel) {
                return new ReadingContent(List.of(new ReadingContent.Paragraph("Ein Testabsatz.", List.of("Ein Testabsatz."))));
            }

            @Override
            public ReadingParagraphClozeGeneration generateReadingParagraphCloze(String topic, List<ReadingPracticeVocabularySeed> vocabulary, String difficultyLevel) {
                return new ReadingParagraphClozeGeneration(List.of());
            }

            @Override
            public ReadingUsedVocabularySelection identifyUsedReadingVocabulary(List<ReadingPracticeVocabularySeed> vocabulary, String readingText) {
                return new ReadingUsedVocabularySelection(vocabulary.stream().map(ReadingPracticeVocabularySeed::surface).toList());
            }

            @Override
            public WritingTopicSelection selectWritingTopicForTextGeneration(List<WritingPracticeVocabularySeed> vocabulary, List<String> previousTopics, String difficultyLevel) {
                return new WritingTopicSelection("Test writing topic");
            }

            @Override
            public WritingBilingualContent generateWritingBilingualContent(String topic, List<WritingPracticeVocabularySeed> vocabulary, String difficultyLevel) {
                return new WritingBilingualContent("I write about my daily routine.", "Ich schreibe ueber meinen Alltag.");
            }

            @Override
            public WritingUsedVocabularySelection identifyUsedWritingVocabulary(List<WritingPracticeVocabularySeed> vocabulary, String englishParagraph, String germanParagraph) {
                return new WritingUsedVocabularySelection(vocabulary.stream().map(WritingPracticeVocabularySeed::surface).toList());
            }

            @Override
            public WritingSentencePairSplit splitWritingContentIntoSentencePairs(String englishParagraph, String germanParagraph) {
                return new WritingSentencePairSplit(List.of(new WritingSentencePairSplit.SentencePair(englishParagraph, germanParagraph)));
            }

            @Override
            public WritingSubmissionFeedback generateWritingSubmissionFeedback(String englishParagraph, String referenceGermanParagraph, String submittedGermanParagraph) {
                return new WritingSubmissionFeedback("Test feedback");
            }

            @Override
            public WritingMeaningAnalysisResult analyzeWritingMeaning(String learnerLevel, String englishPrompt, String referenceGermanParagraph, String learnerGermanAnswer) {
                return new WritingMeaningAnalysisResult("partial", List.of("daily routine"), List.of(), List.of(), List.of());
            }

            @Override
            public WritingVocabularyEvaluationResult evaluateWritingVocabulary(String learnerLevel, String englishPrompt, String referenceGermanParagraph, String learnerGermanAnswer, List<WritingFeedbackVocabularyItem> selectedVocabulary, WritingMeaningAnalysisResult meaningAnalysis) {
                return new WritingVocabularyEvaluationResult(selectedVocabulary.stream()
                        .map(item -> new WritingVocabularyEvaluationResult.Item(
                                item.vocabularyId(),
                                item.germanTarget(),
                                WritingVocabularyEvaluationResult.VocabularyStatus.correct,
                                WritingVocabularyEvaluationResult.VocabularyMemorySignal.no_update,
                                item.germanTarget(),
                                "test"
                        ))
                        .toList());
            }

            @Override
            public WritingGrammarIssueDetectionResult detectWritingGrammarIssues(String learnerLevel, String englishPrompt, String referenceGermanParagraph, String learnerGermanAnswer, List<GrammarRuleCatalogItem> grammarCatalog, WritingMeaningAnalysisResult meaningAnalysis, WritingVocabularyEvaluationResult vocabularyEvaluation) {
                return new WritingGrammarIssueDetectionResult(List.of(new WritingGrammarIssueDetectionResult.Issue("", "word_order", 5, "Vielleicht ich gehe", "Vielleicht gehe ich", "Verb position needs practice.", true, 1)));
            }

            @Override
            public WritingStructuredFeedbackResult composeWritingFeedback(String learnerLevel, String englishPrompt, String referenceGermanParagraph, String learnerGermanAnswer, WritingMeaningAnalysisResult meaningAnalysis, WritingVocabularyEvaluationResult vocabularyEvaluation, WritingGrammarIssueDetectionResult grammarIssues, List<WritingGrammarIssueDetectionResult.Issue> selectedTopIssues) {
                return new WritingStructuredFeedbackResult(
                        "Meaning: partial.",
                        "Ich schreibe ueber meinen Alltag.",
                        List.of(new WritingStructuredFeedbackResult.TopFix("Word order", "Vielleicht ich gehe", "Vielleicht gehe ich", "Keep the verb in position 2.")),
                        new WritingStructuredFeedbackResult.VocabularySummary(List.of("Alltag"), List.of()),
                        List.of(new WritingStructuredFeedbackResult.SentenceCorrection("Vielleicht ich gehe.", "Vielleicht gehe ich.", "Verb second.")),
                        List.of(new WritingStructuredFeedbackResult.MicroPracticeItem("Translate: Maybe I go.", "Vielleicht gehe ich.")),
                        "Practice verb-second word order."
                );
            }

            @Override
            public VocabularyClozeBatch generateVocabularyClozeSentences(String topic, List<VocabularyClozeGenerationSeed> vocabulary) {
                return new VocabularyClozeBatch(List.of());
            }

            @Override
            public StudyAnswerEvaluation evaluateStudyAnswer(String sentenceWithBlank, String expectedAnswer, String answerTranslation, String hint, String userAnswer) {
                return new StudyAnswerEvaluation(1.0, 1.0, 1.0, "Test study feedback");
            }

            @Override
            public List<GrammarRuleDraftProposalPort> proposeGrammarRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
                return List.of();
            }

            @Override
            public GrammarRuleDraftDetailsPort generateGrammarRuleDetails(String identifier, String name, String level, String targetLanguage) {
                return new GrammarRuleDraftDetailsPort(identifier, name, level, targetLanguage, List.of("Test explanation."), List.of());
            }
        };
    }
}
