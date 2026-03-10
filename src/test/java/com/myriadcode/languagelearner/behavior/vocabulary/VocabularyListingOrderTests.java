package com.myriadcode.languagelearner.behavior.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.publishers.VocabularyFlashCardPublisher;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
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
    @DisplayName("fetchVocabularies: keeps each group internally sorted by newest creation first")
    void fetchVocabulariesKeepsEachGroupInternallySortedByNewestCreationFirst() {
        var repo = new InMemoryVocabularyRepo(seedVocabulary("user-a", 32));
        var service = new VocabularyOrchestrationService(
                repo,
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                Clock.fixed(Instant.parse("2026-03-09T12:34:00Z"), ZoneOffset.UTC)
        );

        var responses = service.fetchVocabularies("user-a");
        var firstFourGroups = partitionIds(responses, 8);

        assertThat(responses).hasSize(32);
        assertThat(firstFourGroups.subList(0, 3))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        List.of("vocab-32", "vocab-31", "vocab-30", "vocab-29", "vocab-28", "vocab-27", "vocab-26", "vocab-25"),
                        List.of("vocab-24", "vocab-23", "vocab-22", "vocab-21", "vocab-20", "vocab-19", "vocab-18", "vocab-17"),
                        List.of("vocab-16", "vocab-15", "vocab-14", "vocab-13", "vocab-12", "vocab-11", "vocab-10", "vocab-9")
                ));
        assertThat(firstFourGroups.get(3))
                .containsExactly("vocab-8", "vocab-7", "vocab-6", "vocab-5", "vocab-4", "vocab-3", "vocab-2", "vocab-1");
    }

    @Test
    @DisplayName("fetchVocabularies: keeps the same first-three-group order within a minute and changes it next minute")
    void fetchVocabulariesUsesMinuteBasedStatelessGroupShuffle() {
        var vocabularies = seedVocabulary("user-a", 32);
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
        assertThat(nextMinuteOrder.subList(24, 32))
                .containsExactly("vocab-8", "vocab-7", "vocab-6", "vocab-5", "vocab-4", "vocab-3", "vocab-2", "vocab-1");
    }

    private static List<List<String>> partitionIds(List<VocabularyResponse> responses, int size) {
        var ids = responses.stream()
                .map(VocabularyResponse::id)
                .toList();
        var groups = new java.util.ArrayList<List<String>>();
        for (int start = 0; start < ids.size(); start += size) {
            groups.add(ids.subList(start, Math.min(start + size, ids.size())));
        }
        return groups;
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
