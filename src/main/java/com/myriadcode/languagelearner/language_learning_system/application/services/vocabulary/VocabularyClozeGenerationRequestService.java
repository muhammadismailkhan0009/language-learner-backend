package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CLOZE;

@Service
public class VocabularyClozeGenerationRequestService {
    private final VocabularyClozeGenerationService generationService;
    private final ContentGenerationJobService jobService;
    private final VocabularyClozeGenerationProvider provider;

    public VocabularyClozeGenerationRequestService(
            VocabularyClozeGenerationService generationService,
            ContentGenerationJobService jobService,
            @Value("${content-generation.vocabulary-cloze-provider:MCP}") String provider
    ) {
        this.generationService = generationService;
        this.jobService = jobService;
        this.provider = VocabularyClozeGenerationProvider.MCP;
    }

    public GenerateVocabularyClozeSentencesResponse request(String userId) {
        if (provider == VocabularyClozeGenerationProvider.LLM_API) {
            return generationService.generate(userId);
        }

        jobService.createOrReplace(userId, VOCABULARY_CLOZE);
        return new GenerateVocabularyClozeSentencesResponse(0);
    }
}
