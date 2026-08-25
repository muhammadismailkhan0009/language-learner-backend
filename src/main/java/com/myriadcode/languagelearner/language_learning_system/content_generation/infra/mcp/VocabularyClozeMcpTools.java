package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeBatch;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyClozeGenerationService;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CLOZE;

@Service
public class VocabularyClozeMcpTools {
    private final ContentGenerationJobService jobService;
    private final VocabularyClozeGenerationService clozeGenerationService;

    public VocabularyClozeMcpTools(
            ContentGenerationJobService jobService,
            VocabularyClozeGenerationService clozeGenerationService
    ) {
        this.jobService = jobService;
        this.clozeGenerationService = clozeGenerationService;
    }

    @McpTool(
            name = "get_vocabulary_cloze_generation",
            description = "Fetch pending vocabulary cloze generation prompt. Call before generating cloze results."
    )
    public String getVocabularyClozeGeneration() {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, VOCABULARY_CLOZE);
        return clozeGenerationService.preparePrompt(userId);
    }

    @McpTool(
            name = "store_vocabulary_cloze_generation",
            description = "Store generated vocabulary cloze results using exact requested JSON schema."
    )
    @Transactional
    public GenerateVocabularyClozeSentencesResponse storeVocabularyClozeGeneration(
           @McpToolParam VocabularyClozeBatch generated
    ) {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, VOCABULARY_CLOZE);
        var response = clozeGenerationService.store(userId, generated);
        jobService.delete(userId);
        return response;
    }
}
