package com.myriadcode.languagelearner.behavior.vocabulary;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeSentenceResult;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentReadingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentWritingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyClozeGenerationService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyClozeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VocabularyClozeGenerationServiceTests {

    private InMemoryVocabularyRepo vocabularyRepo;
    private FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private FetchRecentReadingTopicsApi recentReadingTopicsApi;
    private FetchRecentWritingTopicsApi recentWritingTopicsApi;
    private VocabularyClozeLlmApi vocabularyClozeLlmApi;
    private VocabularyClozeGenerationService service;

    @BeforeEach
    void setUp() {
        vocabularyRepo = new InMemoryVocabularyRepo();
        flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
        recentReadingTopicsApi = mock(FetchRecentReadingTopicsApi.class);
        recentWritingTopicsApi = mock(FetchRecentWritingTopicsApi.class);
        vocabularyClozeLlmApi = mock(VocabularyClozeLlmApi.class);

        service = new VocabularyClozeGenerationService(
                vocabularyRepo,
                flashcardReviewsApi,
                recentReadingTopicsApi,
                recentWritingTopicsApi,
                vocabularyClozeLlmApi
        );
    }

    @Test
    @DisplayName("generate: fails fast when user has no flashcards")
    void generateFailsWhenNoFlashcards() {
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of());

        assertThatThrownBy(() -> service.generate("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No vocabulary flashcards found for user");
    }

    @Test
    @DisplayName("generate: uses the latest reading and writing topics and stores one cloze sentence per vocabulary")
    void generateStoresClozeSentencePerVocabulary() {
        var vocabulary = seedVocabulary("v-1", "user-1");
        vocabularyRepo.save(vocabulary);

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1"))
                .thenReturn(List.of(new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true)));
        when(recentReadingTopicsApi.findRecentTopics("user-1", 1)).thenReturn(List.of("Travel plans"));
        when(recentWritingTopicsApi.findRecentTopics("user-1", 1)).thenReturn(List.of("Office small talk"));
        when(vocabularyClozeLlmApi.generateClozeSentences(eq("Travel plans | Office small talk"), any()))
                .thenReturn(List.of(new VocabularyClozeSentenceResult(
                        "gehen",
                        "Ich ___ morgen nach Berlin.",
                        "go",
                        "gehe",
                        List.of("gehe"),
                        "go"
                )));

        GenerateVocabularyClozeSentencesResponse response = service.generate("user-1");

        assertThat(response.generatedCount()).isEqualTo(1);
        assertThat(vocabularyRepo.findByIdAndUserId("v-1", "user-1")).isPresent();
        assertThat(vocabularyRepo.findByIdAndUserId("v-1", "user-1").orElseThrow().clozeSentence()).isNotNull();
        assertThat(vocabularyRepo.findByIdAndUserId("v-1", "user-1").orElseThrow().clozeSentence().answerWords())
                .containsExactly("gehe");
        verify(vocabularyClozeLlmApi).generateClozeSentences(eq("Travel plans | Office small talk"), any());
    }

    @Test
    @DisplayName("generate: falls back to general topic when no recent topics exist")
    void generateFallsBackToGeneralTopic() {
        vocabularyRepo.save(seedVocabulary("v-2", "user-1"));

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1"))
                .thenReturn(List.of(new VocabularyFlashcardReviewRecord("f-2", "v-2", State.NEW, true)));
        when(recentReadingTopicsApi.findRecentTopics("user-1", 1)).thenReturn(List.of());
        when(recentWritingTopicsApi.findRecentTopics("user-1", 1)).thenReturn(List.of());
        when(vocabularyClozeLlmApi.generateClozeSentences(eq("General practice"), any()))
                .thenReturn(List.of(new VocabularyClozeSentenceResult(
                        "gehen",
                        "Wir ___ heute Abend.",
                        "learn",
                        "lernen",
                        List.of("lernen"),
                        "learn"
                )));

        service.generate("user-1");

        verify(vocabularyClozeLlmApi).generateClozeSentences(eq("General practice"), any());
    }

    @Test
    @DisplayName("generate: skips vocabulary that already has a cloze sentence and fills from another eligible vocabulary")
    void generateSkipsVocabularyWithExistingClozeSentence() {
        vocabularyRepo.save(withCloze(seedVocabulary("v-1", "user-1"), "Ich ___ schon.", "gehe"));
        vocabularyRepo.save(seedVocabulary("v-2", "user-1"));

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1"))
                .thenReturn(List.of(
                        new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true),
                        new VocabularyFlashcardReviewRecord("f-2", "v-2", State.REVIEW, true)
                ));
        when(recentReadingTopicsApi.findRecentTopics("user-1", 1)).thenReturn(List.of("Daily routines"));
        when(recentWritingTopicsApi.findRecentTopics("user-1", 1)).thenReturn(List.of());
        when(vocabularyClozeLlmApi.generateClozeSentences(eq("Daily routines"), any()))
                .thenReturn(List.of(new VocabularyClozeSentenceResult(
                        "gehen",
                        "Wir ___ heute Abend.",
                        "learn",
                        "lernen",
                        List.of("lernen"),
                        "learn"
                )));

        var response = service.generate("user-1");

        assertThat(response.generatedCount()).isEqualTo(1);
        assertThat(vocabularyRepo.findByIdAndUserId("v-1", "user-1").orElseThrow().clozeSentence().answerText())
                .isEqualTo("gehe");
        assertThat(vocabularyRepo.findByIdAndUserId("v-2", "user-1").orElseThrow().clozeSentence().answerText())
                .isEqualTo("lernen");
        verify(vocabularyClozeLlmApi).generateClozeSentences(
                eq("Daily routines"),
                eq(List.of(new VocabularyClozeGenerationSeed("v-2", "gehen", "to go")))
        );
    }

    @Test
    @DisplayName("generate: returns zero when every reversed vocabulary already has a cloze sentence")
    void generateReturnsZeroWhenAllCandidatesAlreadyHaveClozeSentence() {
        vocabularyRepo.save(withCloze(seedVocabulary("v-1", "user-1"), "Ich ___ schon.", "gehe"));

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1"))
                .thenReturn(List.of(new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true)));

        var response = service.generate("user-1");

        assertThat(response.generatedCount()).isEqualTo(0);
        verify(vocabularyClozeLlmApi, never()).generateClozeSentences(any(), any());
    }

    private Vocabulary seedVocabulary(String vocabularyId, String userId) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(vocabularyId),
                new UserId(userId),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                null,
                List.of(new VocabularyExampleSentence(
                        new VocabularyExampleSentence.VocabularyExampleSentenceId("ex-" + vocabularyId),
                        "Ich gehe nach Hause.",
                        "I go home."
                )),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    private Vocabulary withCloze(Vocabulary vocabulary,
                                 String clozeText,
                                 String answerText) {
        return new Vocabulary(
                vocabulary.id(),
                vocabulary.userId(),
                vocabulary.surface(),
                vocabulary.translation(),
                vocabulary.entryKind(),
                vocabulary.notes(),
                vocabulary.exampleSentences(),
                new VocabularyClozeSentence(
                        new VocabularyClozeSentence.VocabularyClozeSentenceId(UUID.randomUUID().toString()),
                        clozeText,
                        "go",
                        answerText,
                        List.of(answerText),
                        "go"
                ),
                vocabulary.createdAt()
        );
    }

    private static final class InMemoryVocabularyRepo implements VocabularyRepo {
        private final Map<String, Vocabulary> store = new HashMap<>();

        @Override
        public Vocabulary save(Vocabulary vocabulary) {
            store.put(vocabulary.id().id(), vocabulary);
            return vocabulary;
        }

        @Override
        public Optional<Vocabulary> findByIdAndUserId(String vocabularyId, String userId) {
            return Optional.ofNullable(store.get(vocabularyId))
                    .filter(vocabulary -> vocabulary.userId().id().equals(userId));
        }

        @Override
        public Optional<Vocabulary> findById(String vocabularyId) {
            return Optional.ofNullable(store.get(vocabularyId));
        }

        @Override
        public List<Vocabulary> findByUserId(String userId) {
            return store.values().stream()
                    .filter(vocabulary -> vocabulary.userId().id().equals(userId))
                    .toList();
        }

        @Override
        public List<Vocabulary> findByIds(List<String> vocabularyIds) {
            return store.values().stream()
                    .filter(vocabulary -> vocabularyIds.contains(vocabulary.id().id()))
                    .toList();
        }

        @Override
        public Vocabulary replaceClozeSentence(String vocabularyId, String userId, Vocabulary vocabularyWithUpdatedCloze) {
            store.put(vocabularyId, vocabularyWithUpdatedCloze);
            return vocabularyWithUpdatedCloze;
        }
    }
}
