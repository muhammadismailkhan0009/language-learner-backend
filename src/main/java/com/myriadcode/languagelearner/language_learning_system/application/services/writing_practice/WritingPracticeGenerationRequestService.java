package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.springframework.stereotype.Service;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WRITING_PRACTICE;

@Service
public class WritingPracticeGenerationRequestService {
    private final ContentGenerationJobService jobService;

    public WritingPracticeGenerationRequestService(ContentGenerationJobService jobService) {
        this.jobService = jobService;
    }

    public String request(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
        jobService.createOrReplace(userId.trim(), WRITING_PRACTICE);
        return "Writing exercise generation requested. Run your MCP tool.";
    }
}
