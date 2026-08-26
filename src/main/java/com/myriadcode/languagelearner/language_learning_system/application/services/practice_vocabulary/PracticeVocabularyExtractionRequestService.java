package com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyExtractionRequestRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.PRACTICE_VOCABULARY_EXTRACTION;

@Service
public class PracticeVocabularyExtractionRequestService {
    private final PracticeVocabularyExtractionRequestRepo requestRepo;
    private final ContentGenerationJobService jobService;
    private final Clock clock = Clock.systemUTC();

    public PracticeVocabularyExtractionRequestService(PracticeVocabularyExtractionRequestRepo requestRepo,
                                                      ContentGenerationJobService jobService) {
        this.requestRepo = requestRepo;
        this.jobService = jobService;
    }

    @Transactional
    public String request(String userId, String text) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text is required");
        var normalizedUserId = userId.trim();
        requestRepo.save(new PracticeVocabularyExtractionRequest(
                new UserId(normalizedUserId), text.trim(), clock.instant()));
        jobService.createOrReplace(normalizedUserId, PRACTICE_VOCABULARY_EXTRACTION);
        return "Vocabulary extraction requested. Run your MCP tool.";
    }
}
