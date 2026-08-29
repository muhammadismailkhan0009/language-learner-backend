package com.myriadcode.languagelearner.behavior.content_generation;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ContentGenerationJobIdentityTests {
    private final InMemoryJobRepo repo = new InMemoryJobRepo();
    private final ContentGenerationJobService service = new ContentGenerationJobService(repo);

    @Test
    void differentTypesCoexistAndSameTypeIsReplaced() {
        service.createOrReplace("user-1", ContentGenerationJobType.READING_PRACTICE);
        service.createOrReplace("user-1", ContentGenerationJobType.WRITING_PRACTICE);
        service.createOrReplace("user-1", ContentGenerationJobType.READING_PRACTICE);

        assertThat(repo.jobs).hasSize(2);
        assertThat(service.exists("user-1", ContentGenerationJobType.READING_PRACTICE)).isTrue();
        assertThat(service.exists("user-1", ContentGenerationJobType.WRITING_PRACTICE)).isTrue();
    }

    @Test
    void deletingOneTypeRetainsSiblingType() {
        service.createOrReplace("user-1", ContentGenerationJobType.READING_PRACTICE);
        service.createOrReplace("user-1", ContentGenerationJobType.WRITING_PRACTICE);

        service.delete("user-1", ContentGenerationJobType.READING_PRACTICE);

        assertThat(service.exists("user-1", ContentGenerationJobType.READING_PRACTICE)).isFalse();
        assertThat(service.exists("user-1", ContentGenerationJobType.WRITING_PRACTICE)).isTrue();
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

        private static String key(String userId, ContentGenerationJobType type) {
            return userId + ":" + type;
        }
    }
}
