package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public interface VocabularyClozeLlmApi {

    List<VocabularyClozeSentenceResult> generateClozeSentences(String topic,
                                                               List<VocabularyClozeGenerationSeed> vocabulary);
}
