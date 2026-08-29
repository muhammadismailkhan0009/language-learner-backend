package com.myriadcode.languagelearner.behavior.vocabulary_extraction;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyCandidateSelection;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionCandidateRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CANDIDATE_EXTRACTION;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_DETAIL_GENERATION;
import static org.assertj.core.api.Assertions.assertThat;

class VocabularyCandidateStoreFlowTests {
    private final RequestRepo requests = new RequestRepo();
    private final CandidateRepo candidates = new CandidateRepo();
    private final JobRepo jobs = new JobRepo();
    private final ContentGenerationJobService jobService = new ContentGenerationJobService(jobs);

    @Test
    void filtersBlanksDuplicatesAndExistingVocabularyThenHandsOffDetailJob() {
        requests.save(request("scenario-1"));
        jobService.createOrReplace("user-1", VOCABULARY_CANDIDATE_EXTRACTION);
        var service = service(List.of(vocabulary("Haus")));

        var result = service.storeCandidates("user-1", new VocabularyCandidateSelection("request-scenario-1", List.of(
                new VocabularyCandidateSelection.Candidate(" Bahnhof "),
                new VocabularyCandidateSelection.Candidate("bahnhof"),
                new VocabularyCandidateSelection.Candidate("eine   Fahrkarte"),
                new VocabularyCandidateSelection.Candidate("Haus"),
                new VocabularyCandidateSelection.Candidate(" "))));

        assertThat(result.received()).isEqualTo(5);
        assertThat(result.stored()).isEqualTo(2);
        assertThat(candidates.values).extracting(VocabularyExtractionCandidate::surface)
                .containsExactly("Bahnhof", "eine Fahrkarte");
        assertThat(jobService.exists("user-1", VOCABULARY_CANDIDATE_EXTRACTION)).isFalse();
        assertThat(jobService.exists("user-1", VOCABULARY_DETAIL_GENERATION)).isTrue();
    }

    @Test
    void keepsCandidateJobWhileAnotherScenarioRequestRemains() {
        requests.save(request("scenario-1"));
        requests.save(request("scenario-2"));
        jobService.createOrReplace("user-1", VOCABULARY_CANDIDATE_EXTRACTION);

        service(List.of()).storeCandidates("user-1",
                new VocabularyCandidateSelection("request-scenario-1", List.of()));

        assertThat(jobService.exists("user-1", VOCABULARY_CANDIDATE_EXTRACTION)).isTrue();
        assertThat(jobService.exists("user-1", VOCABULARY_DETAIL_GENERATION)).isFalse();
    }

    private VocabularyExtractionService service(List<Vocabulary> vocabulary) {
        return new VocabularyExtractionService(requests, candidates, vocabularyRepo(vocabulary),
                jobService, null, null);
    }

    private VocabularyExtractionRequest request(String scenarioId) {
        return new VocabularyExtractionRequest(
                new VocabularyExtractionRequest.VocabularyExtractionRequestId("request-" + scenarioId),
                new UserId("user-1"), "Text for " + scenarioId, Instant.now());
    }

    private Vocabulary vocabulary(String surface) {
        return new Vocabulary(new Vocabulary.VocabularyId("v-" + surface), new UserId("user-1"), surface,
                "translation", Vocabulary.EntryKind.WORD, "", List.of(), null, Instant.now());
    }

    private VocabularyRepo vocabularyRepo(List<Vocabulary> values) {
        return new VocabularyRepo() {
            public Vocabulary save(Vocabulary value) { return value; }
            public Optional<Vocabulary> findByIdAndUserId(String id, String userId) { return Optional.empty(); }
            public Optional<Vocabulary> findById(String id) { return Optional.empty(); }
            public List<Vocabulary> findByUserId(String userId) { return values; }
            public List<Vocabulary> findByIds(List<String> ids) { return List.of(); }
            public Vocabulary replaceClozeSentence(String id, String userId, Vocabulary value) { return value; }
        };
    }

    private static final class RequestRepo implements VocabularyExtractionRequestRepo {
        private final Map<String, VocabularyExtractionRequest> values = new HashMap<>();
        public VocabularyExtractionRequest save(VocabularyExtractionRequest value) { values.put(value.id().id(), value); return value; }
        public Optional<VocabularyExtractionRequest> findOldestByUserId(String userId) { return values.values().stream().findFirst(); }
        public Optional<VocabularyExtractionRequest> findByIdAndUserId(String requestId, String userId) { return Optional.ofNullable(values.get(requestId)); }
        public boolean existsByUserId(String userId) { return !values.isEmpty(); }
        public void delete(VocabularyExtractionRequest.VocabularyExtractionRequestId id) { values.values().removeIf(value -> value.id().equals(id)); }
    }

    private static final class CandidateRepo implements VocabularyExtractionCandidateRepo {
        private final List<VocabularyExtractionCandidate> values = new ArrayList<>();
        public List<VocabularyExtractionCandidate> saveAll(List<VocabularyExtractionCandidate> added) { values.addAll(added); return added; }
        public List<VocabularyExtractionCandidate> findByUserId(String userId) { return List.copyOf(values); }
        public List<VocabularyExtractionCandidate> findPendingByUserId(String userId) { return values.stream().filter(value -> value.createdVocabularyId() == null).toList(); }
        public boolean existsPendingByUserId(String userId) { return values.stream().anyMatch(value -> value.createdVocabularyId() == null); }
    }

    private static final class JobRepo implements ContentGenerationJobRepo {
        private final Map<String, ContentGenerationJob> values = new HashMap<>();
        public ContentGenerationJob save(ContentGenerationJob job) { values.put(key(job.userId(), job.type()), job); return job; }
        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) { return Optional.ofNullable(values.get(key(userId, type))); }
        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) { values.remove(key(userId, type)); }
        private String key(String userId, ContentGenerationJobType type) { return userId + ":" + type; }
    }
}
