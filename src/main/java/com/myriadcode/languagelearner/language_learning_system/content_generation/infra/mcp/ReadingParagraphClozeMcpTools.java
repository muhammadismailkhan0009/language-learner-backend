package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGenerationContext;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.ReadingParagraphClozeService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PARAGRAPH_CLOZE;

@Service
@Slf4j
public class ReadingParagraphClozeMcpTools {
    private final ContentGenerationJobService jobService;
    private final ReadingParagraphClozeService clozeService;

    public ReadingParagraphClozeMcpTools(ContentGenerationJobService jobService,
                                         ReadingParagraphClozeService clozeService) {
        this.jobService = jobService;
        this.clozeService = clozeService;
    }

    @McpTool(
            name = "get_reading_paragraph_cloze_generation",
            description = "Fetch the exact generation prompt for a pending reading paragraph cloze generation."
    )
    public String getReadingParagraphClozeGeneration() {
        var userId = McpUserContextHolder.requireUserId();
        if (!jobService.exists(userId, READING_PARAGRAPH_CLOZE)) {
            return "";
        }
        var context = clozeService.prepareGeneration(userId);
        return clozeService.buildGenerationPrompt(context);
    }

    @McpTool(
            name = "store_reading_paragraph_cloze_generation",
            description = "Validate and store reading paragraph cloze output using the exact generation schema."
    )
    @Transactional
    public ReadingParagraphClozeStoreResponse storeReadingParagraphClozeGeneration(
            @McpToolParam(description = "Generated reading paragraph cloze output", required = true)
            ClozeParagraphGeneration generated
    ) {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, READING_PARAGRAPH_CLOZE);
        try {
            var response = clozeService.storeGeneration(userId, generated);
            jobService.delete(userId, READING_PARAGRAPH_CLOZE);
            return new ReadingParagraphClozeStoreResponse(true, java.util.List.of(), response);
        } catch (IllegalArgumentException exception) {
            log.warn("Reading paragraph cloze validation failed for userId='{}': {}", userId, exception.getMessage());
            return new ReadingParagraphClozeStoreResponse(false, java.util.List.of(exception.getMessage()), null);
        }
    }
}
