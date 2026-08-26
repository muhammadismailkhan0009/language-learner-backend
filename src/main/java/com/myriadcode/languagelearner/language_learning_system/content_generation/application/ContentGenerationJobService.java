package com.myriadcode.languagelearner.language_learning_system.content_generation.application;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class ContentGenerationJobService {
    private final ContentGenerationJobRepo jobRepo;
    private final Clock clock;

    @Autowired
    public ContentGenerationJobService(ContentGenerationJobRepo jobRepo) {
        this(jobRepo, Clock.systemUTC());
    }

    ContentGenerationJobService(ContentGenerationJobRepo jobRepo, Clock clock) {
        this.jobRepo = jobRepo;
        this.clock = clock;
    }

    @Transactional
    public ContentGenerationJob createOrReplace(String userId, ContentGenerationJobType type) {
        return jobRepo.save(new ContentGenerationJob(userId, type, clock.instant()));
    }

    @Transactional(readOnly = true)
    public ContentGenerationJob require(String userId, ContentGenerationJobType expectedType) {
        var job = jobRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No content generation job found for user"));
        if (job.type() != expectedType) {
            throw new IllegalStateException("Content generation job has unexpected type");
        }
        return job;
    }

    @Transactional(readOnly = true)
    public boolean exists(String userId, ContentGenerationJobType expectedType) {
        return jobRepo.findByUserId(userId)
                .map(job -> job.type() == expectedType)
                .orElse(false);
    }

    @Transactional
    public void delete(String userId) {
        jobRepo.deleteByUserId(userId);
    }
}
