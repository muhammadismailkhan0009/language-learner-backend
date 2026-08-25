package com.myriadcode.languagelearner.language_content.application.services.reading_practice;

import com.myriadcode.languagelearner.language_content.application.externals.*;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClozeParagraphLlmAdapter implements ClozeParagraphLlmApi {
    private final LLMPort llmPort;

    public ClozeParagraphLlmAdapter(LLMPort llmPort) { this.llmPort = llmPort; }

    @Override
    public ClozeParagraphGeneration generate(ClozeParagraphGenerationContext context) {
        if (context == null || (context.vocabulary().isEmpty() && context.grammarRules().isEmpty())) {
            return new ClozeParagraphGeneration(List.of());
        }
        var generated = llmPort.generateClozeParagraph(context);
        return generated == null ? new ClozeParagraphGeneration(List.of()) : generated;
    }

    @Override
    public String buildPrompt(ClozeParagraphGenerationContext context) {
        return PromptsGenerator.clozeParagraph(context);
    }
}
