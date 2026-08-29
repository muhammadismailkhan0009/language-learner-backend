package com.myriadcode.languagelearner.behavior.vocabulary_extraction;

import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionRequestRepo;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CANDIDATE_EXTRACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManualVocabularyExtractionSubmissionTests {

    private final RequestRepo requests = new RequestRepo();
    private final JobRepo jobs = new JobRepo();
    private final ContentGenerationJobService jobService = new ContentGenerationJobService(jobs);
    private final VocabularyExtractionService service =
            new VocabularyExtractionService(requests, null, null, jobService, null, null);

    @Test
    void storesMultilingualSourceAndCreatesCandidateExtractionJob() {
        var submitted = service.submit("user-1", "Lina waits for the train.");

        assertThat(requests.values).containsEntry(submitted.id().id(), submitted);
        assertThat(submitted.userId().id()).isEqualTo("user-1");
        assertThat(submitted.sourceText()).isEqualTo("Lina waits for the train.");
        assertThat(jobService.exists("user-1", VOCABULARY_CANDIDATE_EXTRACTION)).isTrue();
    }

    @Test
    void rejectsBlankSourceWithoutCreatingRequestOrJob() {
        assertThatThrownBy(() -> service.submit("user-1", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceText is required");

        assertThat(requests.values).isEmpty();
        assertThat(jobService.exists("user-1", VOCABULARY_CANDIDATE_EXTRACTION)).isFalse();
    }

    @Test
    void queuesMultipleRequestsBehindOneCandidateJobSignal() {
        service.submit("user-1", "First text");
        service.submit("user-1", "Second text");

        assertThat(requests.values).hasSize(2);
        assertThat(jobs.values).hasSize(1);
        assertThat(jobService.exists("user-1", VOCABULARY_CANDIDATE_EXTRACTION)).isTrue();
    }

    private static final class RequestRepo implements VocabularyExtractionRequestRepo {
        private final Map<String, VocabularyExtractionRequest> values = new LinkedHashMap<>();

        public VocabularyExtractionRequest save(VocabularyExtractionRequest request) {
            values.put(request.id().id(), request);
            return request;
        }

        public Optional<VocabularyExtractionRequest> findOldestByUserId(String userId) {
            return values.values().stream().filter(value -> value.userId().id().equals(userId)).findFirst();
        }

        public Optional<VocabularyExtractionRequest> findByIdAndUserId(String requestId, String userId) {
            return Optional.ofNullable(values.get(requestId))
                    .filter(value -> value.userId().id().equals(userId));
        }

        public boolean existsByUserId(String userId) {
            return values.values().stream().anyMatch(value -> value.userId().id().equals(userId));
        }

        public void delete(VocabularyExtractionRequest.VocabularyExtractionRequestId id) {
            values.remove(id.id());
        }
    }

    private static final class JobRepo implements ContentGenerationJobRepo {
        private final Map<String, ContentGenerationJob> values = new LinkedHashMap<>();

        public ContentGenerationJob save(ContentGenerationJob job) {
            values.put(key(job.userId(), job.type()), job);
            return job;
        }

        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) {
            return Optional.ofNullable(values.get(key(userId, type)));
        }

        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) {
            values.remove(key(userId, type));
        }

        private String key(String userId, ContentGenerationJobType type) {
            return userId + ":" + type;
        }
    }
}

