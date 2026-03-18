package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.common.EnvVariableSupplierUtil;
import com.myriadcode.languagelearner.user_management.application.externals.UserInformationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
public class LLMConfig {

    private static final String PRIMARY_USER_EMAIL = "ismailkhan33302@gmail.com";

    private final EnvVariableSupplierUtil envVariableSupplierUtil;
    private final UserInformationApi userInformationApi;

    @Value("${llm.model_demo:${llm.model}}")
    private String demoModel;

    public OpenAiChatModel chatModel() {
        OpenAiApi baseApiModel = OpenAiApi.builder()
                .baseUrl(envVariableSupplierUtil.getLLMBaseUrl())
                .apiKey(envVariableSupplierUtil.getLLMApiKey())
                .completionsPath(envVariableSupplierUtil.getCompletionsPath())
                .build();

        var chatOptions = OpenAiChatOptions.builder().model(resolveModel()).build();
        return OpenAiChatModel.builder()
                .openAiApi(baseApiModel).defaultOptions(chatOptions).build();


    }

    private String resolveModel() {
        var userEmail = LlmUserContextHolder.currentUserId()
                .flatMap(userInformationApi::findUsernameByUserId)
                .orElse("");
        String selectedModel = PRIMARY_USER_EMAIL.equalsIgnoreCase(userEmail)
                ? envVariableSupplierUtil.getLLMModel()
                : demoModel;
        log.info("Resolved LLM model '{}' for email '{}'", selectedModel, userEmail);
        return selectedModel;
    }
}
