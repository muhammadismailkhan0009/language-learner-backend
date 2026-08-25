package com.myriadcode.languagelearner.language_content.infra.llm.adapters;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingContent;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingParagraphSentenceSplit;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingParagraphs;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_content.application.ports.StudyAnswerEvaluation;
import com.myriadcode.languagelearner.language_content.application.ports.GrammarLevelReassignmentProposalPort;
import com.myriadcode.languagelearner.language_content.application.ports.GrammarRuleDraftDetailsPort;
import com.myriadcode.languagelearner.language_content.application.ports.GrammarRuleDraftProposalPort;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGenerationContext;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentInput;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackVocabularyItem;
import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingMeaningAnalysisResult;
import com.myriadcode.languagelearner.language_content.application.ports.WritingBilingualContent;
import com.myriadcode.languagelearner.language_content.application.ports.WritingSentencePairSplit;
import com.myriadcode.languagelearner.language_content.application.externals.WritingStructuredFeedbackResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingVocabularyEvaluationResult;
import com.myriadcode.languagelearner.language_content.application.ports.WritingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeBatch;
import com.myriadcode.languagelearner.language_content.application.ports.WritingSubmissionFeedback;
import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.infra.llm.LLMConfig;
import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import com.myriadcode.languagelearner.language_content.infra.llm.dtos.LLMVocabulary;
import com.myriadcode.languagelearner.language_content.infra.llm.mappers.LLMVocabMapper;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class LLMGenerator implements LLMPort {

    @Autowired
    private LLMConfig chatClient;


    @Override
    public List<Chunk.ChunkData> generateChunks(LangConfigsAdaptive langconfigs,
                                                List<Sentence.SentenceData> sentences,
                                                List<Chunk.ChunkData> previousChunks) {
        var prompt = PromptsGenerator.chunkGenerator(langconfigs, sentences, previousChunks);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        var result = runFastLLM(messages, new ParameterizedTypeReference<List<Chunk.ChunkData>>() {
        });
        return result;
    }

    @Override
    public List<Sentence.SentenceData> generateSentences(LangConfigsAdaptive languageConfigs,
                                                         List<Sentence.SentenceData> previousSentences) {
        var prompt = PromptsGenerator.sentenceGeneratorNew(languageConfigs,previousSentences);

        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        var result = runLLM(messages, new ParameterizedTypeReference<List<Sentence.SentenceData>>() {
        });
        return result;
    }

    @Override
    public List<Vocabulary.VocabularyData> generateVocabulary(LangConfigsAdaptive langconfigs,
                                                              List<Chunk.ChunkData> chunkData,
                                                              List<Sentence.SentenceData> sentences) {
        var prompt = PromptsGenerator.vocabGenerator(langconfigs, chunkData, sentences);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        var llmVocab = runFastLLM(messages, new ParameterizedTypeReference<List<LLMVocabulary>>() {
        });
        var result = LLMVocabMapper.INSTANCE.toDomainVocabulary(llmVocab);
        return result;
    }

    @Override
    public ReadingTopicSelection selectReadingTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                                                     List<String> previousTopics,
                                                                     LanguageLevel difficultyLevel) {
        var prompt = PromptsGenerator.readingTopicSelection(vocabulary, previousTopics, difficultyLevel);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<ReadingTopicSelection>() {
        });
    }

    @Override
    public ReadingContent generateReadingContent(List<ReadingPracticeVocabularySeed> vocabulary,
                                                 List<String> previousScenarioLabels,
                                                 LanguageLevel difficultyLevel,
                                                 List<String> grammarRuleTitles,
                                                 int scenarioCount) {
        var prompt = PromptsGenerator.readingContentParagraphs(vocabulary, previousScenarioLabels,
                difficultyLevel, grammarRuleTitles, scenarioCount);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<ReadingContent>() {
        });
    }

    @Override
    public ClozeParagraphGeneration generateClozeParagraph(ClozeParagraphGenerationContext context) {
        var prompt = PromptsGenerator.clozeParagraph(context);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        var model = chatClient.resolveFastModel();
        log.warn("Calling DeepSeek for reading paragraph cloze generation with fast model='{}'", model);
        return runLLM(messages, new ParameterizedTypeReference<ClozeParagraphGeneration>() {
        }, model);
    }

    @Override
    public ReadingUsedVocabularySelection identifyUsedReadingVocabulary(List<ReadingPracticeVocabularySeed> vocabulary,
                                                                        String readingText) {
        var prompt = PromptsGenerator.readingUsedVocabularySelection(vocabulary, readingText);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<ReadingUsedVocabularySelection>() {
        });
    }

    @Override
    public WritingTopicSelection selectWritingTopicForTextGeneration(List<WritingPracticeVocabularySeed> vocabulary,
                                                                     List<String> previousTopics,
                                                                     LanguageLevel difficultyLevel) {
        var prompt = PromptsGenerator.writingTopicSelection(vocabulary, previousTopics, difficultyLevel);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<WritingTopicSelection>() {
        });
    }

    @Override
    public WritingBilingualContent generateWritingBilingualContent(String topic,
                                                                   List<WritingPracticeVocabularySeed> vocabulary,
                                                                   LanguageLevel difficultyLevel,
                                                                   List<String> grammarRuleTitles) {
        var prompt = PromptsGenerator.writingBilingualContent(topic, vocabulary, difficultyLevel, grammarRuleTitles);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<WritingBilingualContent>() {
        });
    }

    @Override
    public WritingSentencePairSplit splitWritingContentIntoSentencePairs(String englishParagraph,
                                                                         String germanParagraph) {
        var prompt = PromptsGenerator.writingSentencePairSplit(englishParagraph, germanParagraph);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<WritingSentencePairSplit>() {
        });
    }

    @Override
    public WritingSubmissionFeedback generateWritingSubmissionFeedback(String englishParagraph,
                                                                       String referenceGermanParagraph,
                                                                       String submittedGermanParagraph) {
        return generateWritingSubmissionFeedback(englishParagraph, referenceGermanParagraph, submittedGermanParagraph, List.of());
    }

    @Override
    public WritingSubmissionFeedback generateWritingSubmissionFeedback(String englishParagraph,
                                                                      String referenceGermanParagraph,
                                                                      String submittedGermanParagraph,
                                                                      List<GrammarRuleCatalogItem> grammarCatalog) {
        var prompt = PromptsGenerator.writingSubmissionFeedback(
                englishParagraph,
                referenceGermanParagraph,
                submittedGermanParagraph,
                grammarCatalog
        );
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<WritingSubmissionFeedback>() {
        });
    }

    @Override
    public WritingMeaningAnalysisResult analyzeWritingMeaning(String learnerLevel,
                                                              String englishPrompt,
                                                              String referenceGermanParagraph,
                                                              String learnerGermanAnswer) {
        var prompt = PromptsGenerator.writingMeaningAnalyzer(learnerLevel, englishPrompt, referenceGermanParagraph, learnerGermanAnswer);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<WritingMeaningAnalysisResult>() {
        });
    }

    @Override
    public WritingVocabularyEvaluationResult evaluateWritingVocabulary(String learnerLevel,
                                                                       String englishPrompt,
                                                                       String referenceGermanParagraph,
                                                                       String learnerGermanAnswer,
                                                                       List<WritingFeedbackVocabularyItem> selectedVocabulary,
                                                                       WritingMeaningAnalysisResult meaningAnalysis) {
        var prompt = PromptsGenerator.writingVocabularyEvaluator(
                learnerLevel,
                englishPrompt,
                referenceGermanParagraph,
                learnerGermanAnswer,
                selectedVocabulary,
                meaningAnalysis
        );
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<WritingVocabularyEvaluationResult>() {
        });
    }

    @Override
    public WritingGrammarIssueDetectionResult detectWritingGrammarIssues(String learnerLevel,
                                                                         String englishPrompt,
                                                                         String referenceGermanParagraph,
                                                                         String learnerGermanAnswer,
                                                                         List<GrammarRuleCatalogItem> grammarCatalog,
                                                                         WritingMeaningAnalysisResult meaningAnalysis,
                                                                         WritingVocabularyEvaluationResult vocabularyEvaluation) {
        var prompt = PromptsGenerator.writingGrammarIssueDetector(
                learnerLevel,
                englishPrompt,
                referenceGermanParagraph,
                learnerGermanAnswer,
                grammarCatalog,
                meaningAnalysis,
                vocabularyEvaluation
        );
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<WritingGrammarIssueDetectionResult>() {
        });
    }

    @Override
    public WritingStructuredFeedbackResult composeWritingFeedback(String learnerLevel,
                                                                  String englishPrompt,
                                                                  String referenceGermanParagraph,
                                                                  String learnerGermanAnswer,
                                                                  WritingMeaningAnalysisResult meaningAnalysis,
                                                                  WritingVocabularyEvaluationResult vocabularyEvaluation,
                                                                  WritingGrammarIssueDetectionResult grammarIssues,
                                                                  List<WritingGrammarIssueDetectionResult.Issue> selectedTopIssues) {
        var prompt = PromptsGenerator.writingFeedbackComposer(
                learnerLevel,
                englishPrompt,
                referenceGermanParagraph,
                learnerGermanAnswer,
                meaningAnalysis,
                vocabularyEvaluation,
                grammarIssues,
                selectedTopIssues
        );
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<WritingStructuredFeedbackResult>() {
        });
    }

    @Override
    public VocabularyClozeBatch generateVocabularyClozeSentences(String topic,
                                                                 List<VocabularyClozeGenerationSeed> vocabulary) {
        var prompt = PromptsGenerator.vocabularyClozeSentences(topic, vocabulary);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        var model = chatClient.resolveFastModel();
        log.warn("Calling DeepSeek for vocabulary cloze generation with fast model='{}', vocabularyCount={}",
                model, vocabulary == null ? 0 : vocabulary.size());
        return runLLM(messages, new ParameterizedTypeReference<VocabularyClozeBatch>() {
        }, model);
    }

    @Override
    public StudyAnswerEvaluation evaluateStudyAnswer(String sentenceWithBlank,
                                                     String expectedAnswer,
                                                     String answerTranslation,
                                                     String hint,
                                                     String userAnswer) {
        return evaluateStudyAnswer(sentenceWithBlank, expectedAnswer, answerTranslation, hint, userAnswer, List.of());
    }

    @Override
    public StudyAnswerEvaluation evaluateStudyAnswer(String sentenceWithBlank,
                                                     String expectedAnswer,
                                                     String answerTranslation,
                                                     String hint,
                                                     String userAnswer,
                                                     List<GrammarRuleCatalogItem> grammarCatalog) {
        var prompt = PromptsGenerator.studyAnswerEvaluation(
                sentenceWithBlank,
                expectedAnswer,
                answerTranslation,
                hint,
                userAnswer,
                grammarCatalog
        );
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<StudyAnswerEvaluation>() {
        });
    }

    @Override
    public List<GrammarRuleDraftProposalPort> proposeGrammarRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
        var prompt = PromptsGenerator.grammarRuleDrafts(level, targetLanguage, count, existingRules);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<List<GrammarRuleDraftProposalPort>>() {
        });
    }

    @Override
    public GrammarRuleDraftDetailsPort generateGrammarRuleDetails(String identifier, String name, String level, String targetLanguage) {
        var prompt = PromptsGenerator.grammarRuleDetails(identifier, name, level, targetLanguage);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<GrammarRuleDraftDetailsPort>() {
        });
    }

    @Override
    public List<GrammarLevelReassignmentProposalPort> reassignGrammarLevels(List<GrammarLevelReassignmentInput> grammarRules) {
        var prompt = PromptsGenerator.grammarLevelReassignment(grammarRules);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runFastLLM(messages, new ParameterizedTypeReference<List<GrammarLevelReassignmentProposalPort>>() {
        });
    }

    record SystemPrompt(String prompt) {
    }

    record UserPrompt(String userPrompt) {
    }


    private List<Message> generatePrompt(SystemPrompt systemPrompt, UserPrompt userPrompt) {

        List<Message> messages = List.of(new SystemMessage(systemPrompt.prompt),
                new UserMessage(userPrompt.userPrompt));
        return messages;
    }

    private <T> T doRunLLM(List<Message> llmPrompt, ParameterizedTypeReference<T> typeReference, String model) {

        var outputConverter = new BeanOutputConverter<>(typeReference);
        var jsonFormatInstruction = outputConverter.getFormat();
        var promptWithJsonContract = appendJsonOutputContract(llmPrompt, jsonFormatInstruction);

        // DeepSeek Spring AI model path (no OpenAI-specific response-format dependency).
        Prompt prompt = new Prompt(promptWithJsonContract, DeepSeekChatOptions.builder()
                .model(model)
                .build());

        var response = this.chatClient.chatModel().call(prompt);

        return outputConverter.convert(Objects.requireNonNull(response.getResult().getOutput().getText()));

    }

    private List<Message> appendJsonOutputContract(List<Message> messages, String jsonFormatInstruction) {
        String contract = """

                Return output as STRICT JSON only.
                Do not use markdown, code fences, prose, or labels.
                Follow this JSON contract exactly:
                %s
                """.formatted(jsonFormatInstruction);

        boolean appended = false;
        var rebuilt = new java.util.ArrayList<Message>(messages.size());
        for (Message message : messages) {
            if (!appended && message instanceof UserMessage userMessage) {
                rebuilt.add(new UserMessage(userMessage.getText() + contract));
                appended = true;
                continue;
            }
            rebuilt.add(message);
        }

        if (!appended) {
            rebuilt.add(new UserMessage(contract));
        }

        return List.copyOf(rebuilt);
    }

    private static final int MAX_ATTEMPTS = 3;

    private <T> T runLLM(
            List<Message> llmPrompt,
            ParameterizedTypeReference<T> typeReference
    ) {
        return runLLM(llmPrompt, typeReference, chatClient.resolveModelForCurrentUser());
    }

    private <T> T runFastLLM(
            List<Message> llmPrompt,
            ParameterizedTypeReference<T> typeReference
    ) {
        return runLLM(llmPrompt, typeReference, chatClient.resolveFastModel());
    }

    private <T> T runLLM(
            List<Message> llmPrompt,
            ParameterizedTypeReference<T> typeReference,
            String model
    ) {
        RuntimeException lastException = null;
        var responseType = typeReference.getType().getTypeName();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            var startedAt = Instant.now();
            try {
                log.info("LLM call started: model='{}', responseType='{}', attempt={}/{}",
                        model,
                        responseType,
                        attempt,
                        MAX_ATTEMPTS
                );
                var result = doRunLLM(llmPrompt, typeReference, model);
                log.info("LLM call finished: model='{}', responseType='{}', attempt={}/{}, durationMs={}",
                        model,
                        responseType,
                        attempt,
                        MAX_ATTEMPTS,
                        Duration.between(startedAt, Instant.now()).toMillis()
                );
                return result;
            } catch (RuntimeException ex) {
                lastException = ex;
                log.warn("LLM call failed: model='{}', responseType='{}', attempt={}/{}, durationMs={}, error='{}'",
                        model,
                        responseType,
                        attempt,
                        MAX_ATTEMPTS,
                        Duration.between(startedAt, Instant.now()).toMillis(),
                        ex.getMessage()
                );

                if (attempt == MAX_ATTEMPTS) {
                    break;
                }

                // optional: small backoff
                try {
                    Thread.sleep(200L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }

        throw lastException;
    }

    private List<ReadingContent.Paragraph> buildReadingContent(
            ReadingParagraphs paragraphs,
            ReadingParagraphSentenceSplit sentenceSplit
    ) {
        var splitParagraphs = sentenceSplit == null ? List.<ReadingParagraphSentenceSplit.ParagraphSentences>of()
                : sentenceSplit.paragraphs();

        return java.util.stream.IntStream.range(0, paragraphs.paragraphs().size())
                .mapToObj(index -> {
                    var text = paragraphs.paragraphs().get(index);
                    var sentences = splitParagraphs.stream()
                            .filter(entry -> entry.paragraphIndex() == index)
                            .findFirst()
                            .map(ReadingParagraphSentenceSplit.ParagraphSentences::sentences)
                            .orElse(List.of(text));
                    return new ReadingContent.Paragraph(text, sentences);
                })
                .toList();
    }

}
