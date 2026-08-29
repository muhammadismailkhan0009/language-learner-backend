package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyCandidatePrompt;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyCandidateSelection;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyDetailSelection;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionValidationException;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CANDIDATE_EXTRACTION;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_DETAIL_GENERATION;

@Service
@Slf4j
public class VocabularyExtractionMcpTools {
    private final ContentGenerationJobService jobService;
    private final VocabularyExtractionService extractionService;

    public VocabularyExtractionMcpTools(ContentGenerationJobService jobService,
                                               VocabularyExtractionService extractionService) {
        this.jobService = jobService;
        this.extractionService = extractionService;
    }

    @McpTool(name = "get_vocabulary_candidate_extraction",
            description = "Fetch an opaque request ID and prompt for pending vocabulary candidate extraction.")
    public VocabularyCandidatePrompt getCandidateExtraction() {
        var userId = McpUserContextHolder.requireUserId();
        if (!jobService.exists(userId, VOCABULARY_CANDIDATE_EXTRACTION)) return null;
        return extractionService.prepareCandidatePrompt(userId);
    }

    @McpTool(name = "store_vocabulary_candidate_extraction",
            description = "Validate, filter, and store surface-only vocabulary candidates.")
    public VocabularyExtractionStoreResponse storeCandidateExtraction(
            @McpToolParam(description = "Opaque request ID and surface-only candidates", required = true)
            VocabularyCandidateSelection selection) {
        return store(() -> extractionService.storeCandidates(McpUserContextHolder.requireUserId(), selection));
    }

    @McpTool(name = "get_vocabulary_detail_generation",
            description = "Fetch the prompt for pending vocabulary detail generation.")
    public String getDetailGeneration() {
        var userId = McpUserContextHolder.requireUserId();
        if (!jobService.exists(userId, VOCABULARY_DETAIL_GENERATION)) return "";
        return extractionService.prepareDetailPrompt(userId);
    }

    @McpTool(name = "store_vocabulary_detail_generation",
            description = "Validate generated details and create vocabulary and flashcards.")
    public VocabularyExtractionStoreResponse storeDetailGeneration(
            @McpToolParam(description = "Complete candidate details", required = true)
            VocabularyDetailSelection selection) {
        return store(() -> extractionService.storeDetails(McpUserContextHolder.requireUserId(), selection));
    }

    private VocabularyExtractionStoreResponse store(StoreOperation operation) {
        try {
            return new VocabularyExtractionStoreResponse(true, List.of(), operation.execute());
        } catch (VocabularyExtractionValidationException exception) {
            log.warn("Vocabulary extraction validation failed: {}", exception.getMessage());
            return new VocabularyExtractionStoreResponse(false, exception.validationErrors(), null);
        } catch (IllegalArgumentException exception) {
            log.warn("Vocabulary extraction validation failed: {}", exception.getMessage());
            return new VocabularyExtractionStoreResponse(false, List.of(exception.getMessage()), null);
        }
    }

    @FunctionalInterface
    private interface StoreOperation {
        com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionResult execute();
    }
}
