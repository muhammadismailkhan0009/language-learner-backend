package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.common.EnvVariableSupplierUtil;
import com.myriadcode.languagelearner.user_management.application.externals.UserInformationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LLMConfig {

    private static final String PRIMARY_USER_EMAIL = "ismailkhan33302@gmail.com";

    private final EnvVariableSupplierUtil envVariableSupplierUtil;
    private final UserInformationApi userInformationApi;
    private final ChatModel chatModel;

    public ChatModel chatModel() {
        return chatModel;
    }

    public String resolveModelForCurrentUser() {
        var userEmail = LlmUserContextHolder.currentUserId()
                .flatMap(userInformationApi::findUsernameByUserId)
                .orElse("");
        String selectedModel = PRIMARY_USER_EMAIL.equalsIgnoreCase(userEmail)
                ? envVariableSupplierUtil.getLLMModel()
                : envVariableSupplierUtil.getLLMDemoModel();
        log.info("Resolved LLM model '{}' for email '{}'", selectedModel, userEmail);
        return selectedModel;
    }

    public String resolveFastModel() {
        return envVariableSupplierUtil.getLLMFastModel();
    }
}
