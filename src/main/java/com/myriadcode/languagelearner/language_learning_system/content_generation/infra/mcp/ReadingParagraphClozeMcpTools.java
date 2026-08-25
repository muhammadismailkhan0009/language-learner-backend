package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGenerationContext;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.ReadingParagraphClozeService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PARAGRAPH_CLOZE;

@Service
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
            description = "Fetch the exact input context for a pending reading paragraph cloze generation."
    )
    public ClozeParagraphGenerationContext getReadingParagraphClozeGeneration() {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, READING_PARAGRAPH_CLOZE);
        return clozeService.prepareGeneration(userId);
    }

    @McpTool(
            name = "store_reading_paragraph_cloze_generation",
            description = "Validate and store reading paragraph cloze output using the exact generation schema."
    )
    @Transactional
    public ReadingParagraphClozeSessionResponse storeReadingParagraphClozeGeneration(
            @McpToolParam(description = "Generated reading paragraph cloze output", required = true)
            ClozeParagraphGeneration generated
    ) {
        var userId = McpUserContextHolder.requireUserId();
        jobService.require(userId, READING_PARAGRAPH_CLOZE);
        var response = clozeService.storeGeneration(userId, generated);
        jobService.delete(userId);
        return response;
    }
}
