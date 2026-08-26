package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeGeneration;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeGenerationValidationException;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WRITING_PRACTICE;

@Service
@Slf4j
public class WritingPracticeMcpTools {
    private final ContentGenerationJobService jobService;
    private final WritingPracticeService writingPracticeService;

    public WritingPracticeMcpTools(ContentGenerationJobService jobService, WritingPracticeService writingPracticeService) {
        this.jobService = jobService;
        this.writingPracticeService = writingPracticeService;
    }

    @McpTool(name = "get_writing_practice_generation",
            description = "Fetch the exact generation prompt for a pending three-scenario writing exercise.")
    public String getWritingPracticeGeneration() {
        var userId = McpUserContextHolder.requireUserId();
        if (!jobService.exists(userId, WRITING_PRACTICE)) return "";
        return writingPracticeService.prepareGenerationPrompt(userId);
    }

    @McpTool(name = "store_writing_practice_generation",
            description = "Validate and store three-scenario writing exercise output using the exact generation schema.")
    @Transactional
    public WritingPracticeStoreResponse storeWritingPracticeGeneration(
            @McpToolParam(description = "Generated three-scenario writing exercise output", required = true)
            WritingPracticeGeneration generated) {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, WRITING_PRACTICE);
        try {
            writingPracticeService.storeGeneration(userId, generated);
            jobService.delete(userId);
            return new WritingPracticeStoreResponse(true, java.util.List.of());
        } catch (WritingPracticeGenerationValidationException exception) {
            log.warn("Writing practice validation failed for userId='{}': {}", userId, exception.getMessage());
            return new WritingPracticeStoreResponse(false, exception.validationErrors());
        } catch (IllegalArgumentException exception) {
            log.warn("Writing practice validation failed for userId='{}': {}", userId, exception.getMessage());
            return new WritingPracticeStoreResponse(false, java.util.List.of(exception.getMessage()));
        }
    }
}
