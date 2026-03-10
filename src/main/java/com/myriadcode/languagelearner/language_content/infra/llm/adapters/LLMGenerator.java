package com.myriadcode.languagelearner.language_content.infra.llm.adapters;

import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingContent;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingParagraphSentenceSplit;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingParagraphs;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.ports.WritingBilingualContent;
import com.myriadcode.languagelearner.language_content.application.ports.WritingSentencePairSplit;
import com.myriadcode.languagelearner.language_content.application.ports.WritingTopicSelection;
import com.myriadcode.languagelearner.language_content.application.ports.WritingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_content.application.ports.VocabularyClozeBatch;
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
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class LLMGenerator implements LLMPort {

    @Autowired
    private LLMConfig chatClient;


    @Override
    public List<Chunk.ChunkData> generateChunks(LangConfigsAdaptive langconfigs,
                                                List<Sentence.SentenceData> sentences,
                                                List<Chunk.ChunkData> previousChunks) {
        var prompt = PromptsGenerator.chunkGenerator(langconfigs, sentences, previousChunks);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        var result = runLLM(messages, new ParameterizedTypeReference<List<Chunk.ChunkData>>() {
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
        var llmVocab = runLLM(messages, new ParameterizedTypeReference<List<LLMVocabulary>>() {
        });
        var result = LLMVocabMapper.INSTANCE.toDomainVocabulary(llmVocab);
        return result;
    }

    @Override
    public ReadingTopicSelection selectReadingTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                                                     List<String> previousTopics,
                                                                     String difficultyLevel) {
        var prompt = PromptsGenerator.readingTopicSelection(vocabulary, previousTopics, difficultyLevel);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<ReadingTopicSelection>() {
        });
    }

    @Override
    public ReadingContent generateReadingContent(String topic,
                                                 List<ReadingPracticeVocabularySeed> vocabulary,
                                                 String difficultyLevel) {
        var paragraphsPrompt = PromptsGenerator.readingContentParagraphs(topic, vocabulary, difficultyLevel);
        var paragraphsMessages = generatePrompt(new SystemPrompt(""), new UserPrompt(paragraphsPrompt));
        var paragraphs = runLLM(paragraphsMessages, new ParameterizedTypeReference<ReadingParagraphs>() {
        });

        if (paragraphs == null || paragraphs.paragraphs() == null || paragraphs.paragraphs().isEmpty()) {
            return new ReadingContent(List.of());
        }

        var splitPrompt = PromptsGenerator.readingContentParagraphSentenceSplit(paragraphs.paragraphs());
        var splitMessages = generatePrompt(new SystemPrompt(""), new UserPrompt(splitPrompt));
        var sentenceSplit = runLLM(splitMessages, new ParameterizedTypeReference<ReadingParagraphSentenceSplit>() {
        });

        var paragraphList = buildReadingContent(paragraphs, sentenceSplit);
        return new ReadingContent(paragraphList);
    }

    @Override
    public ReadingUsedVocabularySelection identifyUsedReadingVocabulary(List<ReadingPracticeVocabularySeed> vocabulary,
                                                                        String readingText) {
        var prompt = PromptsGenerator.readingUsedVocabularySelection(vocabulary, readingText);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<ReadingUsedVocabularySelection>() {
        });
    }

    @Override
    public WritingTopicSelection selectWritingTopicForTextGeneration(List<WritingPracticeVocabularySeed> vocabulary,
                                                                     List<String> previousTopics,
                                                                     String difficultyLevel) {
        var prompt = PromptsGenerator.writingTopicSelection(vocabulary, previousTopics, difficultyLevel);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<WritingTopicSelection>() {
        });
    }

    @Override
    public WritingBilingualContent generateWritingBilingualContent(String topic,
                                                                   List<WritingPracticeVocabularySeed> vocabulary,
                                                                   String difficultyLevel) {
        var prompt = PromptsGenerator.writingBilingualContent(topic, vocabulary, difficultyLevel);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<WritingBilingualContent>() {
        });
    }

    @Override
    public WritingUsedVocabularySelection identifyUsedWritingVocabulary(List<WritingPracticeVocabularySeed> vocabulary,
                                                                        String englishParagraph,
                                                                        String germanParagraph) {
        var prompt = PromptsGenerator.writingUsedVocabularySelection(vocabulary, englishParagraph, germanParagraph);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<WritingUsedVocabularySelection>() {
        });
    }

    @Override
    public WritingSentencePairSplit splitWritingContentIntoSentencePairs(String englishParagraph,
                                                                         String germanParagraph) {
        var prompt = PromptsGenerator.writingSentencePairSplit(englishParagraph, germanParagraph);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<WritingSentencePairSplit>() {
        });
    }

    @Override
    public VocabularyClozeBatch generateVocabularyClozeSentences(String topic,
                                                                 List<VocabularyClozeGenerationSeed> vocabulary) {
        var prompt = PromptsGenerator.vocabularyClozeSentences(topic, vocabulary);
        var messages = generatePrompt(new SystemPrompt(""), new UserPrompt(prompt));
        return runLLM(messages, new ParameterizedTypeReference<VocabularyClozeBatch>() {
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

    private <T> T doRunLLM(List<Message> llmPrompt, ParameterizedTypeReference<T> typeReference) {

        var outputConverter = new BeanOutputConverter<>(typeReference);

        Prompt prompt = new Prompt(llmPrompt,
                OpenAiChatOptions.builder()
                        .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA,
                                outputConverter.getJsonSchema()))
                        .build());

        var response = this.chatClient.chatModel().call(prompt);

        return outputConverter.convert(Objects.requireNonNull(response.getResult().getOutput().getText()));

    }

    private static final int MAX_ATTEMPTS = 3;

    private <T> T runLLM(
            List<Message> llmPrompt,
            ParameterizedTypeReference<T> typeReference
    ) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return doRunLLM(llmPrompt, typeReference);
            } catch (RuntimeException ex) {
                lastException = ex;

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
