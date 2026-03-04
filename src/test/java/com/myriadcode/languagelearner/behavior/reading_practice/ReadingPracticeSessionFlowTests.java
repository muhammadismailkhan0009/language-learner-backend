package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.repos.ReadingPracticeSessionJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestDbConfigs.class, ReadingPracticeSessionFlowTests.ReadingPracticeTestDoubles.class})
class ReadingPracticeSessionFlowTests {

    @Autowired
    private ReadingPracticeService readingPracticeService;

    @Autowired
    private ReadingPracticeSessionJpaRepo readingPracticeSessionJpaRepo;

    @Autowired
    private StubReadingPracticeLlmApi stubReadingPracticeLlmApi;

    @Autowired
    private StubFetchVocabularyFlashcardReviewsApi stubFetchVocabularyFlashcardReviewsApi;

    @Autowired
    private StubFetchPrivateVocabularyApi stubFetchPrivateVocabularyApi;

    @AfterEach
    void tearDown() {
        readingPracticeSessionJpaRepo.deleteAll();
        stubReadingPracticeLlmApi.lastSeeds = List.of();
        stubReadingPracticeLlmApi.lastTopic = null;
        stubFetchVocabularyFlashcardReviewsApi.reset();
        stubFetchPrivateVocabularyApi.reset();
    }

    @Test
    @DisplayName("createSession: stores generated reading unit in persistent repo")
    void createSessionStoresReadingUnit() {
        readingPracticeService.createSession("user-1");

        var persisted = readingPracticeSessionJpaRepo.findAll();
        assertThat(persisted).hasSize(1);
        var session = readingPracticeSessionJpaRepo.findByIdAndUserId(persisted.getFirst().getId(), "user-1")
                .orElseThrow();
        assertThat(session.getUserId()).isEqualTo("user-1");
        assertThat(session.getTopic()).isEqualTo("topic-1");
        assertThat(session.getReadingText()).isEqualTo("reading text");
        assertThat(session.getVocabularyUsages()).hasSize(10);
        assertThat(stubReadingPracticeLlmApi.lastSeeds).hasSize(10);
    }

    @Test
    @DisplayName("getSession: hydrates flashcard view with reverse direction")
    void getSessionHydratesNonReversedCard() {
        readingPracticeService.createSession("user-1");
        var saved = readingPracticeSessionJpaRepo.findAll().getFirst();

        ReadingPracticeSessionResponse response = readingPracticeService.getSession("user-1", saved.getId());

        assertThat(response.vocabFlashcards()).isNotEmpty();
        assertThat(response.vocabFlashcards())
                .allMatch(card -> !card.isReversed());
        var first = response.vocabFlashcards().getFirst();
        assertThat(first.front().wordOrChunk()).startsWith("surface-");
        assertThat(first.back().wordOrChunk()).startsWith("translation-");
    }

    @Test
    @DisplayName("createSession: uses only non-reversed cards and respects 4/3/2/1 state ratio")
    void createSessionUsesNonReversedCardsWithTargetRatio() {
        var reviews = new ArrayList<VocabularyFlashcardReviewRecord>();
        reviews.add(new VocabularyFlashcardReviewRecord("r-1", "v-r-1", State.REVIEW, false));
        reviews.add(new VocabularyFlashcardReviewRecord("r-2", "v-r-2", State.REVIEW, false));
        reviews.add(new VocabularyFlashcardReviewRecord("r-3", "v-r-3", State.REVIEW, false));
        reviews.add(new VocabularyFlashcardReviewRecord("r-4", "v-r-4", State.REVIEW, false));
        reviews.add(new VocabularyFlashcardReviewRecord("r-reversed", "v-r-reversed", State.REVIEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("rl-1", "v-rl-1", State.RE_LEARNING, false));
        reviews.add(new VocabularyFlashcardReviewRecord("rl-2", "v-rl-2", State.RE_LEARNING, false));
        reviews.add(new VocabularyFlashcardReviewRecord("rl-3", "v-rl-3", State.RE_LEARNING, false));
        reviews.add(new VocabularyFlashcardReviewRecord("rl-reversed", "v-rl-reversed", State.RE_LEARNING, true));
        reviews.add(new VocabularyFlashcardReviewRecord("l-1", "v-l-1", State.LEARNING, false));
        reviews.add(new VocabularyFlashcardReviewRecord("l-2", "v-l-2", State.LEARNING, false));
        reviews.add(new VocabularyFlashcardReviewRecord("l-reversed", "v-l-reversed", State.LEARNING, true));
        reviews.add(new VocabularyFlashcardReviewRecord("n-1", "v-n-1", State.NEW, false));
        reviews.add(new VocabularyFlashcardReviewRecord("n-reversed", "v-n-reversed", State.NEW, true));
        stubFetchVocabularyFlashcardReviewsApi.setReviewsForUser("user-1", reviews);

        var stateByCardId = reviews.stream()
                .filter(review -> !review.isReversed())
                .collect(java.util.stream.Collectors.toMap(
                        VocabularyFlashcardReviewRecord::flashcardId,
                        VocabularyFlashcardReviewRecord::fsrsState
                ));

        readingPracticeService.createSession("user-1");

        var persistedId = readingPracticeSessionJpaRepo.findAll().getFirst().getId();
        var persisted = readingPracticeSessionJpaRepo.findByIdAndUserId(persistedId, "user-1")
                .orElseThrow();
        assertThat(persisted.getVocabularyUsages()).hasSize(10);
        var selectedCardIds = persisted.getVocabularyUsages().stream()
                .map(usage -> usage.getFlashcardId())
                .toList();

        assertThat(selectedCardIds).doesNotContain("r-reversed", "rl-reversed", "l-reversed", "n-reversed");
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.REVIEW)).hasSize(4);
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.RE_LEARNING)).hasSize(3);
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.LEARNING)).hasSize(2);
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.NEW)).hasSize(1);
    }

    @Test
    @DisplayName("listSessions: returns only user's sessions in latest-first order")
    void listSessionsReturnsUserScopedLatestFirst() throws InterruptedException {
        readingPracticeService.createSession("user-1");
        Thread.sleep(5);
        readingPracticeService.createSession("user-1");
        readingPracticeService.createSession("user-2");

        var listed = readingPracticeService.listSessions("user-1");

        assertThat(listed).hasSize(2);
        assertThat(listed.get(0).createdAt()).isAfter(listed.get(1).createdAt());
        assertThat(listed).allMatch(summary -> summary.vocabCount() == 10);
    }

    @Test
    @DisplayName("deleteSession: removes only matching user session")
    void deleteSessionIsUserScoped() {
        readingPracticeService.createSession("user-1");
        var sessionId = readingPracticeSessionJpaRepo.findAll().getFirst().getId();

        readingPracticeService.deleteSession("user-2", sessionId);
        assertThat(readingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1")).isPresent();

        readingPracticeService.deleteSession("user-1", sessionId);
        assertThat(readingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1")).isNotPresent();
    }

    @Test
    @DisplayName("getSession: throws when session belongs to another user")
    void getSessionThrowsWhenUserDoesNotOwnSession() {
        readingPracticeService.createSession("user-1");
        var sessionId = readingPracticeSessionJpaRepo.findAll().getFirst().getId();

        assertThatThrownBy(() -> readingPracticeService.getSession("user-2", sessionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reading session not found");
    }

    @Test
    @DisplayName("detachFlashcard: removes usage and hides flashcard from session response")
    void detachFlashcardRemovesUsage() {
        readingPracticeService.createSession("user-1");
        var sessionId = readingPracticeSessionJpaRepo.findAll().getFirst().getId();
        var usage = readingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1")
                .orElseThrow()
                .getVocabularyUsages()
                .getFirst();
        var flashcardId = usage.getFlashcardId();
        var vocabularyId = usage.getVocabularyId();

        readingPracticeService.detachFlashcard("user-1", sessionId, flashcardId);

        var persisted = readingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1")
                .orElseThrow();
        assertThat(persisted.getVocabularyUsages())
                .noneMatch(saved -> saved.getFlashcardId().equals(flashcardId));
        assertThat(persisted.getVocabularyUsages().size()).isEqualTo(9);
        assertThat(stubFetchPrivateVocabularyApi.getVocabularyRecord(vocabularyId, "user-1")).isNotNull();

        var response = readingPracticeService.getSession("user-1", sessionId);
        assertThat(response.vocabFlashcards())
                .noneMatch(card -> card.id().equals(flashcardId));
        assertThat(response.vocabFlashcards().size()).isEqualTo(9);
    }

    static class ReadingPracticeTestDoubles {

        @Bean
        @Primary
        StubFetchVocabularyFlashcardReviewsApi fetchVocabularyFlashcardReviewsApi() {
            return new StubFetchVocabularyFlashcardReviewsApi();
        }

        @Bean
        @Primary
        StubFetchPrivateVocabularyApi fetchPrivateVocabularyApi() {
            return new StubFetchPrivateVocabularyApi();
        }

        @Bean
        @Primary
        StubReadingPracticeLlmApi stubReadingPracticeLlmApi() {
            return new StubReadingPracticeLlmApi();
        }
    }

    static class StubReadingPracticeLlmApi implements ReadingPracticeLlmApi {

        private List<ReadingPracticeVocabularySeed> lastSeeds = List.of();
        private String lastTopic;

        @Override
        public String selectTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                                   String difficultyLevel) {
            this.lastSeeds = vocabulary;
            return "topic-1";
        }

        @Override
        public String generateReadingText(String topic,
                                          List<ReadingPracticeVocabularySeed> vocabulary,
                                          String difficultyLevel) {
            this.lastTopic = topic;
            return "reading text";
        }
    }

    static class StubFetchVocabularyFlashcardReviewsApi implements FetchVocabularyFlashcardReviewsApi {
        private final Map<String, List<VocabularyFlashcardReviewRecord>> reviewsByUser = new HashMap<>();

        StubFetchVocabularyFlashcardReviewsApi() {
            reset();
        }

        @Override
        public List<VocabularyFlashcardReviewRecord> getVocabularyFlashcardsByUser(String userId) {
            return reviewsByUser.getOrDefault(userId, List.of());
        }

        void setReviewsForUser(String userId, List<VocabularyFlashcardReviewRecord> reviews) {
            reviewsByUser.put(userId, reviews);
        }

        void reset() {
            reviewsByUser.clear();
            var defaults = new ArrayList<VocabularyFlashcardReviewRecord>();
            for (int i = 1; i <= 4; i++) {
                defaults.add(new VocabularyFlashcardReviewRecord("r-" + i, "v-r-" + i, State.REVIEW, false));
            }
            for (int i = 1; i <= 3; i++) {
                defaults.add(new VocabularyFlashcardReviewRecord("rl-" + i, "v-rl-" + i, State.RE_LEARNING, false));
            }
            for (int i = 1; i <= 2; i++) {
                defaults.add(new VocabularyFlashcardReviewRecord("l-" + i, "v-l-" + i, State.LEARNING, false));
            }
            defaults.add(new VocabularyFlashcardReviewRecord("n-1", "v-n-1", State.NEW, false));
            reviewsByUser.put("user-1", defaults);
            reviewsByUser.put("user-2", defaults);
        }
    }

    static class StubFetchPrivateVocabularyApi implements FetchPrivateVocabularyApi {
        @Override
        public PrivateVocabularyRecord getVocabularyRecord(String vocabularyId, String userId) {
            return build(vocabularyId, userId);
        }

        @Override
        public List<PrivateVocabularyRecord> getVocabularyRecords(List<String> vocabularyIds, String userId) {
            return vocabularyIds.stream().map(id -> build(id, userId)).toList();
        }

        void reset() {
        }

        private PrivateVocabularyRecord build(String id, String userId) {
            return new PrivateVocabularyRecord(
                    id,
                    userId,
                    "surface-" + id,
                    "translation-" + id,
                    "WORD",
                    List.of(),
                    Instant.parse("2026-01-01T00:00:00Z")
            );
        }
    }
}
