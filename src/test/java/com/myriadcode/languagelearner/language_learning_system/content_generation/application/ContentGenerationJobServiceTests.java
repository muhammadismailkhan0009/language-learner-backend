package com.myriadcode.languagelearner.language_learning_system.content_generation.application;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentGenerationJobServiceTests {
    private static final Instant NOW = Instant.parse("2026-08-25T12:00:00Z");
    private final InMemoryJobRepo repo = new InMemoryJobRepo();
    private final ContentGenerationJobService service = new ContentGenerationJobService(
            repo, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void createOrReplaceKeepsOneJobPerUser() {
        service.createOrReplace("user-1", ContentGenerationJobType.VOCABULARY_CLOZE);
        service.createOrReplace("user-1", ContentGenerationJobType.VOCABULARY_CLOZE);

        assertThat(repo.jobs).hasSize(1);
        assertThat(service.require("user-1", ContentGenerationJobType.VOCABULARY_CLOZE).createdAt())
                .isEqualTo(NOW);
    }

    @Test
    void jobsRemainIndependentAcrossUsers() {
        service.createOrReplace("user-1", ContentGenerationJobType.VOCABULARY_CLOZE);
        service.createOrReplace("user-2", ContentGenerationJobType.VOCABULARY_CLOZE);

        assertThat(repo.jobs).hasSize(2);
    }

    @Test
    void grammarOperationsKeepIndependentPendingJobs() {
        service.createOrReplace("user-1", ContentGenerationJobType.GRAMMAR_RULE_DRAFT);
        service.createOrReplace("user-1", ContentGenerationJobType.GRAMMAR_RULE_DETAILS);
        service.createOrReplace("user-1", ContentGenerationJobType.GRAMMAR_LEVEL_REASSIGNMENT);

        assertThat(repo.jobs).hasSize(3);
        assertThat(service.exists("user-1", ContentGenerationJobType.GRAMMAR_RULE_DRAFT)).isTrue();
        assertThat(service.exists("user-1", ContentGenerationJobType.GRAMMAR_RULE_DETAILS)).isTrue();
        assertThat(service.exists("user-1", ContentGenerationJobType.GRAMMAR_LEVEL_REASSIGNMENT)).isTrue();
    }

    @Test
    void requireRejectsMissingJob() {
        assertThatThrownBy(() -> service.require("user-1", ContentGenerationJobType.VOCABULARY_CLOZE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No content generation job found for user");
    }

    @Test
    void deleteRemovesUsersJob() {
        service.createOrReplace("user-1", ContentGenerationJobType.VOCABULARY_CLOZE);
        service.delete("user-1", ContentGenerationJobType.VOCABULARY_CLOZE);
        assertThat(repo.findByUserIdAndType("user-1", ContentGenerationJobType.VOCABULARY_CLOZE)).isEmpty();
    }

    private static final class InMemoryJobRepo implements ContentGenerationJobRepo {
        private final Map<String, ContentGenerationJob> jobs = new HashMap<>();

        @Override
        public ContentGenerationJob save(ContentGenerationJob job) {
            jobs.put(key(job.userId(), job.type()), job);
            return job;
        }

        @Override
        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) {
            return Optional.ofNullable(jobs.get(key(userId, type)));
        }

        @Override
        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) {
            jobs.remove(key(userId, type));
        }

        private String key(String userId, ContentGenerationJobType type) {
            return userId + ":" + type;
        }
    }
}
