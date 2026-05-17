package com.myriadcode.languagelearner.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvVariableSupplierUtil {

    @Value("${spring.ai.deepseek.chat.options.model:deepseek-v4-flash}")
    private String model;

    public String getLLMModel() {
        return model;
    }

    @Value("${llm.model-demo:${spring.ai.deepseek.chat.options.model:deepseek-v4-flash}}")
    private String demoModel;

    public String getLLMDemoModel() {
        return demoModel;
    }

    @Value("${llm.model-fast:deepseek-v4-flash}")
    private String fastModel;

    public String getLLMFastModel() {
        return fastModel;
    }
}
