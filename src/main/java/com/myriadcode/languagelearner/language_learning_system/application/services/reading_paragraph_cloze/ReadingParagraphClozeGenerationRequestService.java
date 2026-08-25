package com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeGenerationRequestResponse;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PARAGRAPH_CLOZE;

@Service
@Slf4j
public class ReadingParagraphClozeGenerationRequestService {
    private final ContentGenerationJobService jobService;
    private final ReadingParagraphClozeService clozeService;
    private final String provider;

    public ReadingParagraphClozeGenerationRequestService(
            ContentGenerationJobService jobService,
            ReadingParagraphClozeService clozeService,
            @Value("${content-generation.reading-paragraph-cloze-provider:MCP}") String provider
    ) {
        this.jobService = jobService;
        this.clozeService = clozeService;
        this.provider = provider.trim().toUpperCase();
        log.info("Reading paragraph cloze provider initialized as '{}'", this.provider);
    }

    public ReadingParagraphClozeGenerationRequestResponse request(String userId, Integer limit) {
        if ("LLM_API".equals(provider)) {
            log.warn("Entering LLM_API reading paragraph cloze branch for userId='{}'", userId);
            return new ReadingParagraphClozeGenerationRequestResponse(
                    "Reading paragraph cloze session created.",
                    clozeService.createSession(userId, limit)
            );
        }
        log.info("Creating MCP reading paragraph cloze job for userId='{}'", userId);
        jobService.createOrReplace(userId, READING_PARAGRAPH_CLOZE);
        return new ReadingParagraphClozeGenerationRequestResponse(
                "Reading paragraph cloze generation requested. Run your MCP tool.",
                null
        );
    }
}
