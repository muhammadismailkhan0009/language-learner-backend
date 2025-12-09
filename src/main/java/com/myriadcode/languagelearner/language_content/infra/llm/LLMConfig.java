package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.common.EnvVariableSupplierUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LLMConfig {

    private final EnvVariableSupplierUtil envVariableSupplierUtil;

    public OpenAiChatModel chatModel() {
        OpenAiApi baseApiModel = OpenAiApi.builder()
                .baseUrl(envVariableSupplierUtil.getLLMBaseUrl())
                .apiKey(envVariableSupplierUtil.getLLMApiKey())
                .completionsPath(envVariableSupplierUtil.getCompletionsPath())
                .build();

        var chatOptions = OpenAiChatOptions.builder().model(envVariableSupplierUtil.getLLMModel()).build();
        return OpenAiChatModel.builder()
                .openAiApi(baseApiModel).defaultOptions(chatOptions).build();


    }
}
