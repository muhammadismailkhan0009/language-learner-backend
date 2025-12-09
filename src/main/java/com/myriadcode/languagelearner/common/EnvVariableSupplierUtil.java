package com.myriadcode.languagelearner.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class EnvVariableSupplierUtil {

    @Value("${llm.model}")
    private String model;

    @Value("${llm.completions-path}")
    private String completionsPath;

    @Value("${llm.api-keys}")
    private List<String> apiKeys;

    @Value("${llm.base-url}")
    private String baseUrl;

    // AtomicInteger ensures thread-safety in concurrent environments
    private final AtomicInteger counter = new AtomicInteger(0);

    public String getLLMApiKey() {
        int index = counter.getAndUpdate(i -> (i + 1) % apiKeys.size());
        return apiKeys.get(index);

    }

    public String getCompletionsPath() {
        return completionsPath;
    }

    public String getLLMModel() {
        return model;
    }

    public String getLLMBaseUrl() {
        return baseUrl;
    }
}
