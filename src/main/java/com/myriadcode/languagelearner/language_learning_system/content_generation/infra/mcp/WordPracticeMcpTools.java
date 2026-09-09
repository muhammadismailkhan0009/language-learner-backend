package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.InvalidWordPracticeBatchException;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.WordPracticeCapacityExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WORD_PRACTICE;

@Service
@Slf4j
public class WordPracticeMcpTools {
    private final ContentGenerationJobService jobService;
    private final WordPracticeGenerationService generationService;

    public WordPracticeMcpTools(ContentGenerationJobService jobService,
                                WordPracticeGenerationService generationService) {
        this.jobService = jobService;
        this.generationService = generationService;
    }

    @McpTool(
            name = "get_word_practice_generation",
            description = "Fetch the complete prompt and vocabulary data for pending Word Practice generation. "
                    + "Return an empty string when no Word Practice job is pending."
    )
    public String getWordPracticeGeneration() {
        var userId = McpUserContextHolder.requireUserId();
        if (!jobService.exists(userId, WORD_PRACTICE)) {
            return "";
        }
        return generationService.prepareGenerationPrompt(userId);
    }

    @McpTool(
            name = "store_word_practice_generation",
            description = "Validate and atomically store generated Word Practice exercises. Echo the exact selected "
                    + "candidate metadata returned by get_word_practice_generation in selected, and place generated "
                    + "3-to-5-context groups in groups. Successful storage consumes weak events and deletes the pending job."
    )
    public WordPracticeStoreResponse storeWordPracticeGeneration(
            @McpToolParam(
                    description = "Echoed selected candidates and generated groups for 1 to 10 vocabulary targets",
                    required = true
            ) WordPracticeGeneration generation
    ) {
        var userId = McpUserContextHolder.requireUserId();
        try {
            int stored = generationService.store(userId, generation.selected(), generation.groups());
            return new WordPracticeStoreResponse(true, stored, List.of());
        } catch (InvalidWordPracticeBatchException | WordPracticeCapacityExceededException
                 | IllegalArgumentException exception) {
            log.warn("Word Practice validation failed for userId='{}': {}", userId, exception.getMessage());
            return new WordPracticeStoreResponse(false, 0, List.of(exception.getMessage()));
        }
    }
}
