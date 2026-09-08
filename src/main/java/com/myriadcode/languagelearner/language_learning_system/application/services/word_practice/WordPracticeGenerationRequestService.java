package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services.WordPracticeCapacityPolicy;
import org.springframework.stereotype.Service;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WORD_PRACTICE;
import static com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services.WordPracticeCapacityPolicy.GENERATION_VOCABULARY_COUNT;

@Service
public class WordPracticeGenerationRequestService {
    private final WordPracticeRepo practiceRepo;
    private final ContentGenerationJobService jobService;
    private final WordPracticeCapacityPolicy capacityPolicy = new WordPracticeCapacityPolicy();

    public WordPracticeGenerationRequestService(WordPracticeRepo practiceRepo,
                                                ContentGenerationJobService jobService) {
        this.practiceRepo = practiceRepo;
        this.jobService = jobService;
    }

    public String request(String userId) {
        requireUserId(userId);
        capacityPolicy.requireCapacity(
                practiceRepo.countDistinctActiveVocabulary(userId),
                GENERATION_VOCABULARY_COUNT
        );
        jobService.createOrReplace(userId, WORD_PRACTICE);
        return "Word Practice generation requested. Run your MCP tool.";
    }

    private void requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }
}
