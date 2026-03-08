package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeBilingualContent;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos.WritingPracticeSessionJpaRepo;
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
@Import({TestDbConfigs.class, WritingPracticeSessionFlowTests.WritingPracticeTestDoubles.class})
class WritingPracticeSessionFlowTests {

    @Autowired
    private WritingPracticeService writingPracticeService;

    @Autowired
    private WritingPracticeSessionJpaRepo writingPracticeSessionJpaRepo;

    @Autowired
    private StubWritingPracticeLlmApi stubWritingPracticeLlmApi;

    @Autowired
    private StubFetchVocabularyFlashcardReviewsApi stubFetchVocabularyFlashcardReviewsApi;

    @Autowired
    private StubFetchPrivateVocabularyApi stubFetchPrivateVocabularyApi;

    @AfterEach
    void tearDown() {
        writingPracticeSessionJpaRepo.deleteAll();
        stubWritingPracticeLlmApi.lastSeeds = List.of();
        stubWritingPracticeLlmApi.lastTopic = null;
        stubWritingPracticeLlmApi.lastPreviousTopics = List.of();
        stubWritingPracticeLlmApi.usedSurfacesOverride = null;
        stubFetchVocabularyFlashcardReviewsApi.reset();
        stubFetchPrivateVocabularyApi.reset();
    }

    @Test
    @DisplayName("createSession: stores generated writing unit in persistent repo")
    void createSessionStoresWritingUnit() {
        writingPracticeService.createSession("user-1");

        var persisted = writingPracticeSessionJpaRepo.findAll();
        assertThat(persisted).hasSize(1);
        var session = writingPracticeSessionJpaRepo.findByIdAndUserId(persisted.getFirst().getId(), "user-1").orElseThrow();
        assertThat(session.getUserId()).isEqualTo("user-1");
        assertThat(session.getTopic()).isEqualTo("topic-1");
        assertThat(session.getEnglishParagraph()).isEqualTo("I write about my daily routine.");
        assertThat(session.getGermanParagraph()).isEqualTo("Ich schreibe ueber meinen Alltag.");
        assertThat(session.getSubmittedAnswer()).isNull();
        assertThat(session.getSubmittedAt()).isNull();
        assertThat(session.getSentencePairs()).hasSize(2);
        assertThat(session.getVocabularyUsages()).hasSize(20);
        assertThat(stubWritingPracticeLlmApi.lastSeeds).hasSize(20);
    }

    @Test
    @DisplayName("createSession: passes recent topics to topic selection stage")
    void createSessionPassesRecentTopics() {
        writingPracticeService.createSession("user-1");
        writingPracticeService.createSession("user-1");

        assertThat(stubWritingPracticeLlmApi.lastPreviousTopics).isNotEmpty();
    }

    @Test
    @DisplayName("getSession: hydrates flashcard view with reversed cards")
    void getSessionHydratesReversedCard() {
        writingPracticeService.createSession("user-1");
        var saved = writingPracticeSessionJpaRepo.findAll().getFirst();

        WritingPracticeSessionResponse response = writingPracticeService.getSession("user-1", saved.getId());

        assertThat(response.vocabFlashcards()).isNotEmpty();
        assertThat(response.vocabFlashcards()).allMatch(WritingVocabularyFlashCardView::isReversed);
        var first = response.vocabFlashcards().getFirst();
        assertThat(first.front().wordOrChunk()).startsWith("translation-");
        assertThat(first.back().wordOrChunk()).startsWith("surface-");
    }

    @Test
    @DisplayName("createSession: uses only reversed cards and respects 6/8/4/2 state ratio")
    void createSessionUsesReversedCardsWithTargetRatio() {
        var reviews = new ArrayList<VocabularyFlashcardReviewRecord>();
        reviews.add(new VocabularyFlashcardReviewRecord("r-1", "v-r-1", State.REVIEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("r-2", "v-r-2", State.REVIEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("r-3", "v-r-3", State.REVIEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("r-4", "v-r-4", State.REVIEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("r-5", "v-r-5", State.REVIEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("r-6", "v-r-6", State.REVIEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("r-front", "v-r-front", State.REVIEW, false));
        for (int i = 1; i <= 8; i++) {
            reviews.add(new VocabularyFlashcardReviewRecord("rl-" + i, "v-rl-" + i, State.RE_LEARNING, true));
        }
        for (int i = 1; i <= 4; i++) {
            reviews.add(new VocabularyFlashcardReviewRecord("l-" + i, "v-l-" + i, State.LEARNING, true));
        }
        reviews.add(new VocabularyFlashcardReviewRecord("n-1", "v-n-1", State.NEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("n-2", "v-n-2", State.NEW, true));
        reviews.add(new VocabularyFlashcardReviewRecord("n-front", "v-n-front", State.NEW, false));
        stubFetchVocabularyFlashcardReviewsApi.setReviewsForUser("user-1", reviews);

        var stateByCardId = reviews.stream()
                .filter(VocabularyFlashcardReviewRecord::isReversed)
                .collect(java.util.stream.Collectors.toMap(VocabularyFlashcardReviewRecord::flashcardId,
                        VocabularyFlashcardReviewRecord::fsrsState));

        writingPracticeService.createSession("user-1");

        var persistedId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();
        var persisted = writingPracticeSessionJpaRepo.findByIdAndUserId(persistedId, "user-1").orElseThrow();
        assertThat(persisted.getVocabularyUsages()).hasSize(20);
        var selectedCardIds = persisted.getVocabularyUsages().stream().map(usage -> usage.getFlashcardId()).toList();

        assertThat(selectedCardIds).doesNotContain("r-front", "n-front");
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.REVIEW)).hasSize(6);
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.RE_LEARNING)).hasSize(8);
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.LEARNING)).hasSize(4);
        assertThat(selectedCardIds.stream().filter(id -> stateByCardId.get(id) == State.NEW)).hasSize(2);
    }

    @Test
    @DisplayName("createSession: attaches only flashcards whose vocabulary was used in generated content")
    void createSessionAttachesOnlyUsedVocabulary() {
        stubWritingPracticeLlmApi.usedSurfacesOverride = List.of("surface-v-review-1", "surface-v-learning-1");

        writingPracticeService.createSession("user-1");

        var persistedId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();
        var persisted = writingPracticeSessionJpaRepo.findByIdAndUserId(persistedId, "user-1").orElseThrow();

        assertThat(stubWritingPracticeLlmApi.lastSeeds).hasSize(20);
        assertThat(persisted.getVocabularyUsages())
                .extracting(usage -> usage.getVocabularyId())
                .containsExactlyInAnyOrder("v-review-1", "v-learning-1");
    }

    @Test
    @DisplayName("listSessions: returns only user's sessions in latest-first order")
    void listSessionsReturnsUserScopedLatestFirst() throws InterruptedException {
        writingPracticeService.createSession("user-1");
        Thread.sleep(5);
        writingPracticeService.createSession("user-1");
        writingPracticeService.createSession("user-2");

        var listed = writingPracticeService.listSessions("user-1");

        assertThat(listed).hasSize(2);
        assertThat(listed.getFirst().createdAt()).isAfter(listed.get(1).createdAt());
        assertThat(listed).allMatch(summary -> summary.vocabCount() == 20);
    }

    @Test
    @DisplayName("deleteSession: removes only matching user session")
    void deleteSessionIsUserScoped() {
        writingPracticeService.createSession("user-1");
        var sessionId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();

        writingPracticeService.deleteSession("user-2", sessionId);
        assertThat(writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1")).isPresent();

        writingPracticeService.deleteSession("user-1", sessionId);
        assertThat(writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1")).isNotPresent();
    }

    @Test
    @DisplayName("getSession: throws when session belongs to another user")
    void getSessionThrowsWhenUserDoesNotOwnSession() {
        writingPracticeService.createSession("user-1");
        var sessionId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();

        assertThatThrownBy(() -> writingPracticeService.getSession("user-2", sessionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Writing session not found");
    }

    @Test
    @DisplayName("detachFlashcard: removes usage and hides flashcard from session response")
    void detachFlashcardRemovesUsage() {
        writingPracticeService.createSession("user-1");
        var sessionId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();
        var usage = writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1").orElseThrow().getVocabularyUsages().iterator().next();
        var flashcardId = usage.getFlashcardId();
        var vocabularyId = usage.getVocabularyId();

        writingPracticeService.detachFlashcard("user-1", sessionId, flashcardId);

        var persisted = writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1").orElseThrow();
        assertThat(persisted.getVocabularyUsages()).noneMatch(saved -> saved.getFlashcardId().equals(flashcardId));
        assertThat(persisted.getVocabularyUsages().size()).isEqualTo(19);
        assertThat(stubFetchPrivateVocabularyApi.getVocabularyRecord(vocabularyId, "user-1")).isNotNull();

        var response = writingPracticeService.getSession("user-1", sessionId);
        assertThat(response.vocabFlashcards()).noneMatch(card -> card.id().equals(flashcardId));
        assertThat(response.vocabFlashcards().size()).isEqualTo(19);
    }

    @Test
    @DisplayName("submitAnswer: stores learner answer and exposes it in session response")
    void submitAnswerStoresAnswer() {
        writingPracticeService.createSession("user-1");
        var sessionId = writingPracticeSessionJpaRepo.findAll().getFirst().getId();

        writingPracticeService.submitAnswer("user-1", sessionId, "My written answer");

        var persisted = writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, "user-1").orElseThrow();
        assertThat(persisted.getSubmittedAnswer()).isEqualTo("My written answer");
        assertThat(persisted.getSubmittedAt()).isNotNull();

        var response = writingPracticeService.getSession("user-1", sessionId);
        assertThat(response.submittedAnswer()).isEqualTo("My written answer");
        assertThat(response.submittedAt()).isNotNull();

        var listed = writingPracticeService.listSessions("user-1");
        assertThat(listed).anyMatch(WritingPracticeSessionSummaryResponse::submitted);
    }

    static class WritingPracticeTestDoubles {

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
        StubWritingPracticeLlmApi writingPracticeLlmApi() {
            return new StubWritingPracticeLlmApi();
        }
    }

    static class StubWritingPracticeLlmApi implements WritingPracticeLlmApi {
        private List<WritingPracticeVocabularySeed> lastSeeds = List.of();
        private String lastTopic;
        private List<String> lastPreviousTopics = List.of();
        private List<String> usedSurfacesOverride;

        @Override
        public String selectTopicForWriting(List<WritingPracticeVocabularySeed> vocabulary, List<String> previousTopics, String difficultyLevel) {
            this.lastSeeds = vocabulary;
            this.lastPreviousTopics = previousTopics;
            this.lastTopic = "topic-1";
            return lastTopic;
        }

        @Override
        public WritingPracticeBilingualContent generateBilingualContent(String topic, List<WritingPracticeVocabularySeed> vocabulary, String difficultyLevel) {
            this.lastTopic = topic;
            return new WritingPracticeBilingualContent(
                    "I write about my daily routine.",
                    "Ich schreibe ueber meinen Alltag."
            );
        }

        @Override
        public List<String> identifyUsedVocabulary(List<WritingPracticeVocabularySeed> vocabulary,
                                                   String englishParagraph,
                                                   String germanParagraph) {
            if (usedSurfacesOverride != null) {
                return usedSurfacesOverride;
            }
            return vocabulary.stream().map(WritingPracticeVocabularySeed::surface).toList();
        }

        @Override
        public List<WritingPracticeSentencePairSeed> splitIntoSentencePairs(String englishParagraph, String germanParagraph) {
            return List.of(
                    new WritingPracticeSentencePairSeed("I write about my day.", "Ich schreibe ueber meinen Tag."),
                    new WritingPracticeSentencePairSeed("I practice with new words.", "Ich uebe mit neuen Woertern.")
            );
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
            var defaults = new ArrayList<VocabularyFlashcardReviewRecord>();
            for (int i = 1; i <= 6; i++) {
                defaults.add(new VocabularyFlashcardReviewRecord("review-" + i, "v-review-" + i, State.REVIEW, true));
            }
            for (int i = 1; i <= 8; i++) {
                defaults.add(new VocabularyFlashcardReviewRecord("relearn-" + i, "v-relearn-" + i, State.RE_LEARNING, true));
            }
            for (int i = 1; i <= 4; i++) {
                defaults.add(new VocabularyFlashcardReviewRecord("learning-" + i, "v-learning-" + i, State.LEARNING, true));
            }
            for (int i = 1; i <= 2; i++) {
                defaults.add(new VocabularyFlashcardReviewRecord("new-" + i, "v-new-" + i, State.NEW, true));
            }
            reviewsByUser.clear();
            reviewsByUser.put("user-1", defaults);
            reviewsByUser.put("user-2", defaults);
        }
    }

    static class StubFetchPrivateVocabularyApi implements FetchPrivateVocabularyApi {
        private final Map<String, Map<String, PrivateVocabularyRecord>> byUser = new HashMap<>();

        StubFetchPrivateVocabularyApi() {
            reset();
        }

        @Override
        public PrivateVocabularyRecord getVocabularyRecord(String vocabularyId, String userId) {
            return byUser.getOrDefault(userId, Map.of()).get(vocabularyId);
        }

        @Override
        public List<PrivateVocabularyRecord> getVocabularyRecords(List<String> vocabularyIds, String userId) {
            var records = byUser.getOrDefault(userId, Map.of());
            return vocabularyIds.stream().map(records::get).filter(java.util.Objects::nonNull).toList();
        }

        void reset() {
            byUser.clear();
            seedUser("user-1");
            seedUser("user-2");
        }

        private void seedUser(String userId) {
            var records = new HashMap<String, PrivateVocabularyRecord>();
            for (int i = 1; i <= 6; i++) {
                records.put("v-review-" + i, vocab("v-review-" + i, userId, i));
            }
            for (int i = 1; i <= 8; i++) {
                records.put("v-relearn-" + i, vocab("v-relearn-" + i, userId, 10 + i));
            }
            for (int i = 1; i <= 4; i++) {
                records.put("v-learning-" + i, vocab("v-learning-" + i, userId, 20 + i));
            }
            for (int i = 1; i <= 2; i++) {
                records.put("v-new-" + i, vocab("v-new-" + i, userId, 30 + i));
            }
            for (int i = 1; i <= 8; i++) {
                records.put("v-rl-" + i, vocab("v-rl-" + i, userId, 40 + i));
            }
            for (int i = 1; i <= 4; i++) {
                records.put("v-l-" + i, vocab("v-l-" + i, userId, 50 + i));
            }
            records.put("v-n-1", vocab("v-n-1", userId, 61));
            records.put("v-n-2", vocab("v-n-2", userId, 62));
            for (int i = 1; i <= 6; i++) {
                records.put("v-r-" + i, vocab("v-r-" + i, userId, 70 + i));
            }
            byUser.put(userId, records);
        }

        private PrivateVocabularyRecord vocab(String id, String userId, int minuteOffset) {
            return new PrivateVocabularyRecord(
                    id,
                    userId,
                    "surface-" + id,
                    "translation-" + id,
                    "WORD",
                    List.of(new PrivateVocabularyRecord.ExampleSentenceRecord("ex-" + id, "Beispiel " + id, "Example " + id)),
                    Instant.parse("2026-01-01T00:%02d:00Z".formatted(minuteOffset % 60))
            );
        }
    }
}
