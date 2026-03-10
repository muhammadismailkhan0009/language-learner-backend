package com.myriadcode.languagelearner.language_content.application.services.vocabulary;

import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeSentenceResult;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabularyClozeLlmAdapter implements VocabularyClozeLlmApi {

    private final LLMPort llmPort;

    public VocabularyClozeLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public List<VocabularyClozeSentenceResult> generateClozeSentences(String topic,
                                                                      List<VocabularyClozeGenerationSeed> vocabulary) {
        var result = llmPort.generateVocabularyClozeSentences(topic, vocabulary);
        if (result == null || result.clozeSentences() == null) {
            return List.of();
        }
        return result.clozeSentences().stream()
                .map(item -> new VocabularyClozeSentenceResult(
                        item.vocabSource(),
                        item.clozeText(),
                        item.hint(),
                        item.answerText(),
                        item.answerWords() == null ? List.of() : item.answerWords(),
                        item.answerTranslation()
                ))
                .toList();
    }
}
