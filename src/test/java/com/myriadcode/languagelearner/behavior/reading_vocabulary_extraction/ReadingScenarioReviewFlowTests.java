package com.myriadcode.languagelearner.behavior.reading_vocabulary_extraction;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingFlashcardReviewService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionRequestRepo;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CANDIDATE_EXTRACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReadingScenarioReviewFlowTests {
    @Test
    void finalAcceptedRatingCompletesScenarioAndCreatesExtractionWork() {
        var readingRepo = new ReadingRepo(scenario("card-1"));
        var requests = new RequestRepo();
        var jobs = new ContentGenerationJobService(new JobRepo());
        var reviewed = new boolean[1];
        var service = new ReadingFlashcardReviewService(
                userId -> List.of(new VocabularyFlashcardReviewRecord("card-1", "vocab-1", State.REVIEW, true)),
                (flashcardId, rating) -> reviewed[0] = true,
                null, readingRepo, new VocabularyExtractionService(requests, null, null, jobs, null, null));

        service.review("user-1", "scenario-1", "card-1", Rating.HARD);

        assertThat(reviewed[0]).isTrue();
        assertThat(readingRepo.scenario.ratedCardsCount()).isEqualTo(1);
        assertThat(readingRepo.scenario.allCardsRated()).isTrue();
        assertThat(requests.values).hasSize(1);
        assertThat(requests.values.values().stream().findFirst().orElseThrow().sourceText()).isEqualTo("Text");
        assertThat(jobs.exists("user-1", VOCABULARY_CANDIDATE_EXTRACTION)).isTrue();
    }

    @Test
    void unattachedFlashcardFailsBeforeReviewAndProgress() {
        var readingRepo = new ReadingRepo(scenario("other-card"));
        var reviewed = new boolean[1];
        var service = new ReadingFlashcardReviewService(
                userId -> List.of(new VocabularyFlashcardReviewRecord("card-1", "vocab-1", State.REVIEW, true)),
                (flashcardId, rating) -> reviewed[0] = true,
                null, readingRepo, new VocabularyExtractionService(new RequestRepo(), null, null,
                        new ContentGenerationJobService(new JobRepo()), null, null));

        assertThatThrownBy(() -> service.review("user-1", "scenario-1", "card-1", Rating.HARD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not attached");
        assertThat(reviewed[0]).isFalse();
        assertThat(readingRepo.scenario.ratedCardsCount()).isZero();
    }

    private ReadingPracticeScenario scenario(String flashcardId) {
        return new ReadingPracticeScenario(new ReadingPracticeScenario.ReadingPracticeScenarioId("scenario-1"),
                "Topic", "Text", 0, List.of(), List.of(new ReadingVocabularyUsage(
                new ReadingVocabularyUsage.ReadingVocabularyUsageId("usage-1"), flashcardId, "vocab-1")));
    }

    private static final class ReadingRepo implements ReadingPracticeRepo {
        private ReadingPracticeScenario scenario;
        private ReadingRepo(ReadingPracticeScenario scenario) { this.scenario = scenario; }
        public ReadingPracticeSession save(ReadingPracticeSession session) { return session; }
        public Optional<ReadingPracticeSession> findByIdAndUserId(String sessionId, String userId) { return Optional.empty(); }
        public Optional<ReadingPracticeScenario> findScenarioByIdAndUserIdForUpdate(String scenarioId, String userId) { return Optional.of(scenario); }
        public Optional<ReadingPracticeScenario> findScenarioByIdAndUserId(String scenarioId, String userId) { return Optional.of(scenario); }
        public ReadingPracticeScenario saveScenarioProgress(ReadingPracticeScenario value) { scenario = value; return value; }
        public List<ReadingPracticeSession> findAllByUserId(String userId) { return List.of(); }
        public List<String> findRecentTopicsByUserId(String userId, int limit) { return List.of(); }
        public List<Set<String>> findRecentVocabularyUsageSessionSetsByUserId(String userId, int limit) { return List.of(); }
        public void deleteByIdAndUserId(String sessionId, String userId) {}
        public void detachFlashcard(String userId, String sessionId, String flashcardId) {}
    }

    private static final class RequestRepo implements VocabularyExtractionRequestRepo {
        private final Map<String, VocabularyExtractionRequest> values = new HashMap<>();
        public VocabularyExtractionRequest save(VocabularyExtractionRequest value) { values.put(value.id().id(), value); return value; }
        public Optional<VocabularyExtractionRequest> findOldestByUserId(String userId) { return values.values().stream().findFirst(); }
        public Optional<VocabularyExtractionRequest> findByIdAndUserId(String requestId, String userId) { return Optional.ofNullable(values.get(requestId)); }
        public boolean existsByUserId(String userId) { return !values.isEmpty(); }
        public void delete(VocabularyExtractionRequest.VocabularyExtractionRequestId id) { values.values().removeIf(value -> value.id().equals(id)); }
    }

    private static final class JobRepo implements ContentGenerationJobRepo {
        private final Map<String, ContentGenerationJob> values = new HashMap<>();
        public ContentGenerationJob save(ContentGenerationJob job) { values.put(key(job.userId(), job.type()), job); return job; }
        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) { return Optional.ofNullable(values.get(key(userId, type))); }
        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) { values.remove(key(userId, type)); }
        private String key(String userId, ContentGenerationJobType type) { return userId + ":" + type; }
    }
}
