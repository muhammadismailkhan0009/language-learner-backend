package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeReadingContent;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeValidationException;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PRACTICE;

@Service
@Slf4j
public class ReadingPracticeMcpTools {
    private final ContentGenerationJobService jobService;
    private final ReadingPracticeService readingPracticeService;

    public ReadingPracticeMcpTools(ContentGenerationJobService jobService,
                                   ReadingPracticeService readingPracticeService) {
        this.jobService = jobService;
        this.readingPracticeService = readingPracticeService;
    }

    @McpTool(
            name = "get_reading_practice_generation",
            description = "Fetch the exact generation prompt for a pending reading exercise."
    )
    public String getReadingPracticeGeneration() {
        var userId = McpUserContextHolder.requireUserId();
        if (!jobService.exists(userId, READING_PRACTICE)) {
            return "";
        }
        return readingPracticeService.prepareGenerationPrompt(userId);
    }

    @McpTool(
            name = "store_reading_practice_generation",
            description = "Validate and store reading exercise output using the exact generation schema."
    )
    @Transactional
    public ReadingPracticeStoreResponse storeReadingPracticeGeneration(
            @McpToolParam(description = "Generated reading exercise output", required = true)
            ReadingPracticeReadingContent generated
    ) {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, READING_PRACTICE);
        try {
            readingPracticeService.storeGeneration(userId, generated);
            jobService.delete(userId);
            return new ReadingPracticeStoreResponse(true, List.of());
        } catch (ReadingPracticeValidationException exception) {
            log.warn("Reading practice validation failed for userId='{}': {}", userId, exception.getMessage());
            return new ReadingPracticeStoreResponse(false, exception.validationErrors());
        }
    }
}
