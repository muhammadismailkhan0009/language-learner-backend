package com.myriadcode.languagelearner.behavior.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
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
    @DisplayName("fetchVocabularies: keeps vocabulary within creation-order groups of eight")
    void fetchVocabulariesKeepsVocabularyWithinCreationOrderGroupsOfEight() {
        var repo = new InMemoryVocabularyRepo(seedVocabulary("user-a", 10));
        var service = new VocabularyOrchestrationService(
                repo,
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                Clock.fixed(Instant.parse("2026-03-09T12:34:00Z"), ZoneOffset.UTC)
        );

        var responses = service.fetchVocabularies("user-a");

        assertThat(responses).hasSize(10);
        assertThat(responses.subList(0, 8))
                .extracting(response -> response.id())
                .containsExactlyInAnyOrder("vocab-1", "vocab-2", "vocab-3", "vocab-4",
                        "vocab-5", "vocab-6", "vocab-7", "vocab-8");
        assertThat(responses.subList(8, 10))
                .extracting(response -> response.id())
                .containsExactlyInAnyOrder("vocab-9", "vocab-10");
    }

    @Test
    @DisplayName("fetchVocabularies: returns a stable order within a minute and a different order next minute")
    void fetchVocabulariesUsesMinuteBasedStatelessShuffle() {
        var vocabularies = seedVocabulary("user-a", 16);
        var currentMinuteService = new VocabularyOrchestrationService(
                new InMemoryVocabularyRepo(vocabularies),
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                Clock.fixed(Instant.parse("2026-03-09T12:34:20Z"), ZoneOffset.UTC)
        );
        var sameMinuteService = new VocabularyOrchestrationService(
                new InMemoryVocabularyRepo(vocabularies),
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                Clock.fixed(Instant.parse("2026-03-09T12:34:59Z"), ZoneOffset.UTC)
        );
        var nextMinuteService = new VocabularyOrchestrationService(
                new InMemoryVocabularyRepo(vocabularies),
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                Clock.fixed(Instant.parse("2026-03-09T12:35:00Z"), ZoneOffset.UTC)
        );

        var currentMinuteOrder = currentMinuteService.fetchVocabularies("user-a").stream()
                .map(response -> response.id())
                .toList();
        var sameMinuteOrder = sameMinuteService.fetchVocabularies("user-a").stream()
                .map(response -> response.id())
                .toList();
        var nextMinuteOrder = nextMinuteService.fetchVocabularies("user-a").stream()
                .map(response -> response.id())
                .toList();

        assertThat(currentMinuteOrder).isEqualTo(sameMinuteOrder);
        assertThat(nextMinuteOrder).isNotEqualTo(currentMinuteOrder);
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
    }
}
