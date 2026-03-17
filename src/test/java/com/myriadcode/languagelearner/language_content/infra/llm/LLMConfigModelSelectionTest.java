package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.common.EnvVariableSupplierUtil;
import com.myriadcode.languagelearner.user_management.application.externals.UserInformationApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMConfigModelSelectionTest {

    @Mock
    private EnvVariableSupplierUtil envVariableSupplierUtil;

    @Mock
    private UserInformationApi userInformationApi;

    @Test
    void shouldUsePrimaryModelForConfiguredPrimaryEmail() {
        when(envVariableSupplierUtil.getLLMModel()).thenReturn("primary-model");
        when(userInformationApi.findUsernameByUserId("primary-user"))
                .thenReturn(Optional.of("ismailkhan33302@gmail.com"));

        var config = new LLMConfig(envVariableSupplierUtil, userInformationApi);
        ReflectionTestUtils.setField(config, "demoModel", "demo-model");

        String selectedModel;
        try (var ignored = LlmUserContextHolder.scoped("primary-user")) {
            selectedModel = ReflectionTestUtils.invokeMethod(config, "resolveModel");
        }

        assertThat(selectedModel).isEqualTo("primary-model");
    }

    @Test
    void shouldUseDemoModelForNonPrimaryEmail() {
        when(userInformationApi.findUsernameByUserId("demo-user"))
                .thenReturn(Optional.of("someone-else@gmail.com"));

        var config = new LLMConfig(envVariableSupplierUtil, userInformationApi);
        ReflectionTestUtils.setField(config, "demoModel", "demo-model");

        String selectedModel;
        try (var ignored = LlmUserContextHolder.scoped("demo-user")) {
            selectedModel = ReflectionTestUtils.invokeMethod(config, "resolveModel");
        }

        assertThat(selectedModel).isEqualTo("demo-model");
    }
}
