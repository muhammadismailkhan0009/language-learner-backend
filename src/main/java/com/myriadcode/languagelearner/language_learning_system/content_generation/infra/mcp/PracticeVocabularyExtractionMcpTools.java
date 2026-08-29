package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.ports.ReadingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyExtractionValidationException;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.PRACTICE_VOCABULARY_EXTRACTION;

@Service
@Slf4j
@Deprecated(forRemoval = true)
public class PracticeVocabularyExtractionMcpTools {
    private final ContentGenerationJobService jobService;
    private final PracticeVocabularyService vocabularyService;

    public PracticeVocabularyExtractionMcpTools(ContentGenerationJobService jobService,
                                                PracticeVocabularyService vocabularyService) {
        this.jobService = jobService;
        this.vocabularyService = vocabularyService;
    }

    @McpTool(name = "get_practice_vocabulary_extraction",
            description = "Fetch the exact prompt for a pending practice vocabulary extraction.")
    public String getPracticeVocabularyExtraction() {
        var userId = McpUserContextHolder.requireUserId();
        if (!jobService.exists(userId, PRACTICE_VOCABULARY_EXTRACTION)) return "";
        return vocabularyService.prepareExtractionPrompt(userId);
    }

    @McpTool(name = "store_practice_vocabulary_extraction",
            description = "Validate and store extracted practice vocabulary using the exact selection schema.")
    @Transactional
    public PracticeVocabularyExtractionStoreResponse storePracticeVocabularyExtraction(
            @McpToolParam(description = "Selected vocabulary surfaces", required = true)
            ReadingUsedVocabularySelection selection) {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, PRACTICE_VOCABULARY_EXTRACTION);
        try {
            var result = vocabularyService.storeExtraction(userId, selection);
            vocabularyService.deleteExtractionRequest(userId);
            jobService.delete(userId, PRACTICE_VOCABULARY_EXTRACTION);
            return new PracticeVocabularyExtractionStoreResponse(true, List.of(), result);
        } catch (PracticeVocabularyExtractionValidationException exception) {
            log.warn("Practice vocabulary extraction validation failed for userId='{}': {}", userId, exception.getMessage());
            return new PracticeVocabularyExtractionStoreResponse(false, exception.validationErrors(), null);
        } catch (IllegalArgumentException exception) {
            log.warn("Practice vocabulary extraction validation failed for userId='{}': {}", userId, exception.getMessage());
            return new PracticeVocabularyExtractionStoreResponse(false, List.of(exception.getMessage()), null);
        }
    }
}
