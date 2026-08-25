package com.myriadcode.languagelearner.language_content.application.externals;

public interface ClozeParagraphLlmApi {
    ClozeParagraphGeneration generate(ClozeParagraphGenerationContext context);

    default String buildPrompt(ClozeParagraphGenerationContext context) {
        throw new UnsupportedOperationException("Cloze paragraph prompt generation is not available");
    }
}
