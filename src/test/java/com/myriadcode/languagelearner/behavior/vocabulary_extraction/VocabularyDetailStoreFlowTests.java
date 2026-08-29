package com.myriadcode.languagelearner.behavior.vocabulary_extraction;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyDetailSelection;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionCandidateRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_DETAIL_GENERATION;
import static org.assertj.core.api.Assertions.assertThat;

class VocabularyDetailStoreFlowTests {
    @Test
    void createsVocabularyLinksCandidateAndClearsFinalDetailJob() {
        var vocabularyRepo = new VocabularyMemoryRepo();
        var candidateRepo = new CandidateMemoryRepo(candidate());
        var jobRepo = new JobMemoryRepo();
        var jobs = new ContentGenerationJobService(jobRepo);
        jobs.createOrReplace("user-1", VOCABULARY_DETAIL_GENERATION);
        var service = new VocabularyExtractionService(null, candidateRepo, vocabularyRepo,
                jobs, null, new VocabularyOrchestrationService(vocabularyRepo));

        var result = service.storeDetails("user-1", new VocabularyDetailSelection(List.of(
                new VocabularyDetailSelection.Detail("candidate-1", "Bahnhof", "train station", "WORD",
                        "masculine noun", List.of(new VocabularyDetailSelection.ExampleSentence(
                        "Ich warte am Bahnhof.", "I am waiting at the train station."))))));

        assertThat(result.created()).isEqualTo(1);
        assertThat(vocabularyRepo.values).hasSize(1);
        assertThat(vocabularyRepo.values.getFirst().surface()).isEqualTo("Bahnhof");
        assertThat(candidateRepo.values.getFirst().createdVocabularyId()).isEqualTo(vocabularyRepo.values.getFirst().id().id());
        assertThat(jobs.exists("user-1", VOCABULARY_DETAIL_GENERATION)).isFalse();
    }

    private VocabularyExtractionCandidate candidate() {
        return new VocabularyExtractionCandidate(
                new VocabularyExtractionCandidate.VocabularyExtractionCandidateId("candidate-1"),
                new UserId("user-1"), "Bahnhof", "bahnhof", null, Instant.now());
    }

    private static final class CandidateMemoryRepo implements VocabularyExtractionCandidateRepo {
        private final List<VocabularyExtractionCandidate> values = new ArrayList<>();
        private CandidateMemoryRepo(VocabularyExtractionCandidate value) { values.add(value); }
        public List<VocabularyExtractionCandidate> saveAll(List<VocabularyExtractionCandidate> added) {
            for (var value : added) { values.removeIf(old -> old.id().equals(value.id())); values.add(value); }
            return added;
        }
        public List<VocabularyExtractionCandidate> findByUserId(String userId) { return List.copyOf(values); }
        public List<VocabularyExtractionCandidate> findPendingByUserId(String userId) { return values.stream().filter(value -> value.createdVocabularyId() == null).toList(); }
        public boolean existsPendingByUserId(String userId) { return values.stream().anyMatch(value -> value.createdVocabularyId() == null); }
    }

    private static final class VocabularyMemoryRepo implements VocabularyRepo {
        private final List<Vocabulary> values = new ArrayList<>();
        public Vocabulary save(Vocabulary value) { values.removeIf(old -> old.id().equals(value.id())); values.add(value); return value; }
        public Optional<Vocabulary> findByIdAndUserId(String id, String userId) { return values.stream().filter(value -> id.equals(value.id().id()) && userId.equals(value.userId().id())).findFirst(); }
        public Optional<Vocabulary> findById(String id) { return values.stream().filter(value -> id.equals(value.id().id())).findFirst(); }
        public List<Vocabulary> findByUserId(String userId) { return values.stream().filter(value -> userId.equals(value.userId().id())).toList(); }
        public List<Vocabulary> findByIds(List<String> ids) { return values.stream().filter(value -> ids.contains(value.id().id())).toList(); }
        public Vocabulary replaceClozeSentence(String id, String userId, Vocabulary value) { return save(value); }
    }

    private static final class JobMemoryRepo implements ContentGenerationJobRepo {
        private final Map<String, ContentGenerationJob> values = new HashMap<>();
        public ContentGenerationJob save(ContentGenerationJob job) { values.put(key(job.userId(), job.type()), job); return job; }
        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) { return Optional.ofNullable(values.get(key(userId, type))); }
        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) { values.remove(key(userId, type)); }
        private String key(String userId, ContentGenerationJobType type) { return userId + ":" + type; }
    }
}
