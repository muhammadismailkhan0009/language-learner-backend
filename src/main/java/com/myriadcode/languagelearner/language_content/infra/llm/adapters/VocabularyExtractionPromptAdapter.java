package com.myriadcode.languagelearner.language_content.infra.llm.adapters;

import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyDetailSeed;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyExtractionPromptApi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabularyExtractionPromptAdapter implements VocabularyExtractionPromptApi {
    @Override
    public String candidatePrompt(String sourceText) {
        return PromptsGenerator.vocabularyCandidateExtraction(sourceText);
    }

    @Override
    public String detailPrompt(List<VocabularyDetailSeed> candidates) {
        return PromptsGenerator.vocabularyDetailGeneration(candidates);
    }
}
