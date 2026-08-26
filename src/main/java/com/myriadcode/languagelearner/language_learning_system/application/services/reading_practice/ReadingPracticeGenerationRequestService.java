package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeGenerationRequestResponse;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PRACTICE;

@Service
public class ReadingPracticeGenerationRequestService {
    private final ReadingPracticeService readingPracticeService;
    private final ContentGenerationJobService jobService;
    private final String provider;

    public ReadingPracticeGenerationRequestService(
            ReadingPracticeService readingPracticeService,
            ContentGenerationJobService jobService,
            @Value("${content-generation.reading-practice-provider:MCP}") String provider
    ) {
        this.readingPracticeService = readingPracticeService;
        this.jobService = jobService;
        this.provider = provider.trim().toUpperCase();
    }

    public ReadingPracticeGenerationRequestResponse request(String userId) {
        if ("LLM_API".equals(provider)) {
            readingPracticeService.createSession(userId);
            return new ReadingPracticeGenerationRequestResponse("Reading exercise created.");
        }
        jobService.createOrReplace(userId, READING_PRACTICE);
        return new ReadingPracticeGenerationRequestResponse(
                "Reading exercise generation requested. Run your MCP tool."
        );
    }
}
