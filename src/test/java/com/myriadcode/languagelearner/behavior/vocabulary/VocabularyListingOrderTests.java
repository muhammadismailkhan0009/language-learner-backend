package com.myriadcode.languagelearner.behavior.vocabulary;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.publishers.VocabularyFlashCardPublisher;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyListingOrderTests {

    @Test
    @DisplayName("fetchVocabularies: orders weak vocabulary before learning and strong entries")
    void fetchVocabulariesOrdersWeakLearningStrong() {
        var vocabularies = seedVocabulary("user-a", 4);
        var service = new VocabularyOrchestrationService(
                new InMemoryVocabularyRepo(vocabularies),
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                statsApi(List.of(
                        review("card-strong", "vocab-1", State.REVIEW, "2026-03-12T15:00:00Z", 8.0, 2.0, 0, "2026-03-08T09:00:00Z"),
                        review("card-new", "vocab-2", State.NEW, null, 0.0, 0.0, 0, null),
                        review("card-learning", "vocab-3", State.LEARNING, "2026-03-09T13:00:00Z", 3.5, 6.0, 0, "2026-03-09T11:00:00Z"),
                        review("card-weak", "vocab-4", State.REVIEW, "2026-03-09T11:00:00Z", 2.0, 7.5, 2, "2026-03-08T08:00:00Z")
                )),
                Clock.fixed(Instant.parse("2026-03-09T12:34:00Z"), ZoneOffset.UTC)
        );

        var responses = service.fetchVocabularies("user-a");

        assertThat(ids(responses))
                .containsExactly("vocab-4", "vocab-3", "vocab-2", "vocab-1");
    }

    @Test
    @DisplayName("fetchVocabularies: uses the most urgent flashcard when a vocabulary has multiple cards")
    void fetchVocabulariesUsesMostUrgentFlashcardPerVocabulary() {
        var vocabularies = seedVocabulary("user-a", 3);
        var service = new VocabularyOrchestrationService(
                new InMemoryVocabularyRepo(vocabularies),
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                statsApi(List.of(
                        review("card-a-front", "vocab-1", State.REVIEW, "2026-03-09T16:00:00Z", 7.0, 3.0, 0, "2026-03-08T10:00:00Z"),
                        review("card-a-reverse", "vocab-1", State.REVIEW, "2026-03-09T11:30:00Z", 1.5, 8.0, 3, "2026-03-07T10:00:00Z"),
                        review("card-b", "vocab-2", State.LEARNING, "2026-03-09T12:45:00Z", 4.5, 5.0, 0, "2026-03-08T11:00:00Z"),
                        review("card-c", "vocab-3", State.REVIEW, "2026-03-09T17:00:00Z", 9.0, 1.0, 0, "2026-03-09T09:00:00Z")
                )),
                Clock.fixed(Instant.parse("2026-03-09T12:34:00Z"), ZoneOffset.UTC)
        );

        assertThat(ids(service.fetchVocabularies("user-a")))
                .containsExactly("vocab-1", "vocab-2", "vocab-3");
    }

    @Test
    @DisplayName("fetchVocabularies: falls back to newest creation first when no flashcard stats exist")
    void fetchVocabulariesFallsBackToNewestCreationFirstWithoutFlashcardStats() {
        var service = new VocabularyOrchestrationService(
                new InMemoryVocabularyRepo(seedVocabulary("user-a", 4)),
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                statsApi(List.of()),
                Clock.fixed(Instant.parse("2026-03-09T12:34:00Z"), ZoneOffset.UTC)
        );

        assertThat(ids(service.fetchVocabularies("user-a")))
                .containsExactly("vocab-4", "vocab-3", "vocab-2", "vocab-1");
    }

    private static List<String> ids(List<VocabularyResponse> responses) {
        return responses.stream()
                .map(VocabularyResponse::id)
                .toList();
    }

    private static FetchVocabularyFlashcardReviewsApi statsApi(List<VocabularyFlashcardReviewRecord> stats) {
        return userId -> stats;
    }

    private static VocabularyFlashcardReviewRecord review(String flashcardId,
                                                          String vocabularyId,
                                                          State state,
                                                          String due,
                                                          double stability,
                                                          double difficulty,
                                                          int lapses,
                                                          String lastReview) {
        return new VocabularyFlashcardReviewRecord(
                flashcardId,
                vocabularyId,
                state,
                due == null ? null : Instant.parse(due),
                stability,
                difficulty,
                lapses,
                lastReview == null ? null : Instant.parse(lastReview),
                false
        );
    }

    private static List<Vocabulary> seedVocabulary(String userId, int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(index -> new Vocabulary(
                        new Vocabulary.VocabularyId("vocab-" + index),
                        new UserId(userId),
                        "surface-" + index,
                        "translation-" + index,
                        Vocabulary.EntryKind.WORD,
                        null,
                        List.of(new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("example-" + index),
                                "Sentence " + index,
                                "Translation " + index
                        )),
                        null,
                        Instant.parse("2026-01-01T00:00:00Z").plusSeconds(index)
                ))
                .toList()
                .reversed();
    }

    private static class InMemoryVocabularyRepo implements VocabularyRepo {
        private final List<Vocabulary> vocabularies;

        private InMemoryVocabularyRepo(List<Vocabulary> vocabularies) {
            this.vocabularies = vocabularies;
        }

        @Override
        public Vocabulary save(Vocabulary vocabulary) {
            throw new UnsupportedOperationException("save is not used in this test");
        }

        @Override
        public Optional<Vocabulary> findByIdAndUserId(String vocabularyId, String userId) {
            return vocabularies.stream()
                    .filter(vocabulary -> vocabulary.id().id().equals(vocabularyId))
                    .filter(vocabulary -> vocabulary.userId().id().equals(userId))
                    .findFirst();
        }

        @Override
        public Optional<Vocabulary> findById(String vocabularyId) {
            return vocabularies.stream()
                    .filter(vocabulary -> vocabulary.id().id().equals(vocabularyId))
                    .findFirst();
        }

        @Override
        public List<Vocabulary> findByUserId(String userId) {
            return vocabularies.stream()
                    .filter(vocabulary -> vocabulary.userId().id().equals(userId))
                    .toList();
        }

        @Override
        public List<Vocabulary> findByIds(List<String> vocabularyIds) {
            return vocabularies.stream()
                    .filter(vocabulary -> vocabularyIds.contains(vocabulary.id().id()))
                    .toList();
        }

        @Override
        public Vocabulary replaceClozeSentence(String vocabularyId, String userId, Vocabulary vocabularyWithUpdatedCloze) {
            throw new UnsupportedOperationException("replaceClozeSentence is not used in this test");
        }
    }
}
