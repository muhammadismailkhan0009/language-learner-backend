package com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeGenerationRequestResponse;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PARAGRAPH_CLOZE;

@Service
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
    }

    public ReadingParagraphClozeGenerationRequestResponse request(String userId, Integer limit) {
        if ("LLM_API".equals(provider)) {
            return new ReadingParagraphClozeGenerationRequestResponse(
                    "Reading paragraph cloze session created.",
                    clozeService.createSession(userId, limit)
            );
        }
        jobService.createOrReplace(userId, READING_PARAGRAPH_CLOZE);
        return new ReadingParagraphClozeGenerationRequestResponse(
                "Reading paragraph cloze generation requested. Run your MCP tool.",
                null
        );
    }
}
