package com.myriadcode.languagelearner.language_content.infra.llm.adapters;

import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
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
    public List<Sentence.SentenceData> generateSentences(LangConfigsAdaptive languageConfigs) {
        var prompt = PromptsGenerator.sentenceGeneratorNew(languageConfigs);

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

    record SystemPrompt(String prompt) {
    }

    record UserPrompt(String userPrompt) {
    }


    private List<Message> generatePrompt(SystemPrompt systemPrompt, UserPrompt userPrompt) {

        List<Message> messages = List.of(new SystemMessage(systemPrompt.prompt),
                new UserMessage(userPrompt.userPrompt));
        return messages;
    }

    private <T> T runLLM(List<Message> llmPrompt, ParameterizedTypeReference<T> typeReference) {

        var outputConverter = new BeanOutputConverter<>(typeReference);

        Prompt prompt = new Prompt(llmPrompt,
                OpenAiChatOptions.builder()
                        .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA,
                                outputConverter.getJsonSchema()))
                        .build());

        var response = this.chatClient.chatModel().call(prompt);

        return outputConverter.convert(Objects.requireNonNull(response.getResult().getOutput().getText()));

    }
}
