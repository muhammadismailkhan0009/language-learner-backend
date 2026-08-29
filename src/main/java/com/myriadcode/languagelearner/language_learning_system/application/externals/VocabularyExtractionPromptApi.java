package com.myriadcode.languagelearner.language_learning_system.application.externals;

import java.util.List;

public interface VocabularyExtractionPromptApi {
    String candidatePrompt(String sourceText);
    String detailPrompt(List<VocabularyDetailSeed> candidates);
}
