package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.publishers.VocabularyFlashCardPublisher;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyClozeGenerationService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VocabularyControllerTests {

    @Test
    @DisplayName("Add vocabulary API: stores item and returns localized examples")
    public void addVocabularyStoresAndReturnsResponse() throws Exception {
        var repo = new FakeVocabularyRepo();
        VocabularyOrchestrationService service = new VocabularyOrchestrationService(repo);
        var controller = new VocabularyController(service, org.mockito.Mockito.mock(VocabularyClozeGenerationService.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        var payload = """
                {
                  "surface": "auf jeden Fall",
                  "translation": "definitely",
                  "entryKind": "CHUNK",
                  "notes": "used for strong agreement",
                  "exampleSentences": [
                    {"sentence": "Auf jeden Fall komme ich.", "translation": "I am definitely coming."}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/vocabularies/v1")
                        .queryParam("userId", "user-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response.id").isNotEmpty())
                .andExpect(jsonPath("$.response.surface").value("auf jeden Fall"))
                .andExpect(jsonPath("$.response.userId").value("user-a"))
                .andExpect(jsonPath("$.response.entryKind").value("CHUNK"))
                .andExpect(jsonPath("$.response.exampleSentences[0].sentence")
                        .value("Auf jeden Fall komme ich."))
                .andExpect(jsonPath("$.response.exampleSentences[0].translation")
                        .value("I am definitely coming."));
    }

    @Test
    @DisplayName("Update and fetch vocabulary API: applies authoritative example updates")
    public void updateAndFetchVocabularyReturnsUpdatedPayload() throws Exception {
        var repo = new FakeVocabularyRepo();
        var seed = sampleVocabulary("vocab-1", "user-a");
        repo.save(seed);

        VocabularyOrchestrationService service = new VocabularyOrchestrationService(repo);
        var controller = new VocabularyController(service, org.mockito.Mockito.mock(VocabularyClozeGenerationService.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        var existingExampleId = seed.exampleSentences().get(0).id().id();
        var updatePayload = """
                {
                  "translation": "to go (updated)",
                  "notes": "updated by test",
                  "exampleSentences": [
                    {
                      "id": "%s",
                      "sentence": "Ich gehe jetzt nach Hause.",
                      "translation": "I am going home now."
                    },
                    {
                      "id": null,
                      "sentence": "Wir gehen zusammen.",
                      "translation": "We go together."
                    }
                  ]
                }
                """.formatted(existingExampleId);

        mockMvc.perform(put("/api/v1/vocabularies/{vocabularyId}/v1", "vocab-1")
                        .queryParam("userId", "user-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.translation").value("to go (updated)"))
                .andExpect(jsonPath("$.response.notes").value("updated by test"))
                .andExpect(jsonPath("$.response.exampleSentences.length()").value(2));

        mockMvc.perform(get("/api/v1/vocabularies/{vocabularyId}/v1", "vocab-1")
                        .queryParam("userId", "user-a")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.id").value("vocab-1"))
                .andExpect(jsonPath("$.response.exampleSentences.length()").value(2));
    }

    @Test
    @DisplayName("Fetch vocabularies API: returns only current user's vocab")
    public void fetchVocabulariesReturnsOnlyCurrentUserData() throws Exception {
        var repo = new FakeVocabularyRepo();
        repo.save(sampleVocabulary("vocab-1", "user-a"));
        repo.save(sampleVocabulary("vocab-2", "user-b"));
        VocabularyOrchestrationService service = new VocabularyOrchestrationService(repo);
        var controller = new VocabularyController(service, org.mockito.Mockito.mock(VocabularyClozeGenerationService.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/vocabularies/v1")
                        .queryParam("userId", "user-a")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(1))
                .andExpect(jsonPath("$.response[0].id").value("vocab-1"))
                .andExpect(jsonPath("$.response[0].userId").value("user-a"));
    }

    @Test
    @DisplayName("Generate cloze sentences API: delegates to explicit generation service")
    public void generateClozeSentencesDelegatesToService() throws Exception {
        var repo = new FakeVocabularyRepo();
        var vocabularyService = new VocabularyOrchestrationService(repo);
        var clozeService = org.mockito.Mockito.mock(VocabularyClozeGenerationService.class);
        when(clozeService.generate("user-a")).thenReturn(new GenerateVocabularyClozeSentencesResponse(3));

        var controller = new VocabularyController(vocabularyService, clozeService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/vocabularies/cloze-sentences/v1")
                        .queryParam("userId", "user-a")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.generatedCount").value(3));
    }

    @Test
    @DisplayName("Song vocab selection API: applies 5:2:3 split oldest-first with fallback fill")
    void fetchSongVocabulariesAppliesConfiguredSplit() throws Exception {
        var repo = new FakeVocabularyRepo();
        seedSeries(repo, "user-a", "new-", 25, State.NEW, Instant.parse("2026-01-01T00:00:00Z"));
        seedSeries(repo, "user-a", "learning-", 10, State.LEARNING, Instant.parse("2026-02-01T00:00:00Z"));
        seedSeries(repo, "user-a", "relearning-", 5, State.RE_LEARNING, Instant.parse("2026-03-01T00:00:00Z"));

        var fetchReviewsApi = buildReviewsApi(repo.findByUserId("user-a"), Map.of(
                "new-", State.NEW,
                "learning-", State.LEARNING,
                "relearning-", State.RE_LEARNING
        ));

        var service = new VocabularyOrchestrationService(
                repo,
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                fetchReviewsApi,
                Clock.fixed(Instant.parse("2026-04-01T00:00:00Z"), ZoneOffset.UTC)
        );
        var controller = new VocabularyController(service, org.mockito.Mockito.mock(VocabularyClozeGenerationService.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/vocabularies/songs-selection/v1")
                        .queryParam("userId", "user-a")
                        .queryParam("limit", "50")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(40))
                .andExpect(jsonPath("$.response[0].id").value("new-1"))
                .andExpect(jsonPath("$.response[24].id").value("new-25"))
                .andExpect(jsonPath("$.response[25].id").value("learning-1"))
                .andExpect(jsonPath("$.response[34].id").value("learning-10"))
                .andExpect(jsonPath("$.response[35].id").value("relearning-1"))
                .andExpect(jsonPath("$.response[39].id").value("relearning-5"));
    }

    @Test
    @DisplayName("Song vocab selection API: defaults to 50 and caps requested limit at 300")
    void fetchSongVocabulariesDefaultAndCapLimit() throws Exception {
        var repo = new FakeVocabularyRepo();
        seedSeries(repo, "user-a", "new-", 220, State.NEW, Instant.parse("2026-01-01T00:00:00Z"));
        seedSeries(repo, "user-a", "learning-", 120, State.LEARNING, Instant.parse("2026-02-01T00:00:00Z"));
        seedSeries(repo, "user-a", "relearning-", 120, State.RE_LEARNING, Instant.parse("2026-03-01T00:00:00Z"));

        var fetchReviewsApi = buildReviewsApi(repo.findByUserId("user-a"), Map.of(
                "new-", State.NEW,
                "learning-", State.LEARNING,
                "relearning-", State.RE_LEARNING
        ));
        var service = new VocabularyOrchestrationService(
                repo,
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                fetchReviewsApi,
                Clock.fixed(Instant.parse("2026-04-01T00:00:00Z"), ZoneOffset.UTC)
        );
        var controller = new VocabularyController(service, org.mockito.Mockito.mock(VocabularyClozeGenerationService.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/vocabularies/songs-selection/v1")
                        .queryParam("userId", "user-a")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(50));

        mockMvc.perform(get("/api/v1/vocabularies/songs-selection/v1")
                        .queryParam("userId", "user-a")
                        .queryParam("limit", "500")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(300));
    }

    private FetchVocabularyFlashcardReviewsApi buildReviewsApi(List<Vocabulary> vocabularies,
                                                               Map<String, State> stateByPrefix) {
        return userId -> {
            var result = new ArrayList<VocabularyFlashcardReviewRecord>();
            for (var vocabulary : vocabularies) {
                var state = stateByPrefix.entrySet().stream()
                        .filter(entry -> vocabulary.id().id().startsWith(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(State.REVIEW);
                result.add(new VocabularyFlashcardReviewRecord(
                        "card-" + vocabulary.id().id(),
                        vocabulary.id().id(),
                        state,
                        true
                ));
            }
            return result;
        };
    }

    private void seedSeries(FakeVocabularyRepo repo,
                            String userId,
                            String prefix,
                            int size,
                            State state,
                            Instant baseCreatedAt) {
        for (int i = 1; i <= size; i++) {
            var id = prefix + i;
            var createdAt = baseCreatedAt.plusSeconds(i);
            repo.save(new Vocabulary(
                    new Vocabulary.VocabularyId(id),
                    new UserId(userId),
                    "surface-" + id,
                    "translation-" + id,
                    Vocabulary.EntryKind.WORD,
                    "state:" + state.name(),
                    List.of(new VocabularyExampleSentence(
                            new VocabularyExampleSentence.VocabularyExampleSentenceId("ex-" + id),
                            "Sentence " + id,
                            "Translation " + id
                    )),
                    null,
                    createdAt
            ));
        }
    }

    private Vocabulary sampleVocabulary(String id, String userId) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(id),
                new UserId(userId),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                "base note",
                List.of(
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("ex-1"),
                                "Ich gehe nach Hause.",
                                "I go home."
                        ),
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("ex-2"),
                                "Sie gehen in die Schule.",
                                "They go to school."
                        )
                ),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    private static class FakeVocabularyRepo implements VocabularyRepo {
        private final Map<String, Vocabulary> data = new HashMap<>();

        @Override
        public Vocabulary save(Vocabulary vocabulary) {
            data.put(vocabulary.id().id(), vocabulary);
            return vocabulary;
        }

        @Override
        public Optional<Vocabulary> findByIdAndUserId(String vocabularyId, String userId) {
            var vocab = data.get(vocabularyId);
            if (vocab == null || !vocab.userId().id().equals(userId)) {
                return Optional.empty();
            }
            return Optional.of(vocab);
        }

        @Override
        public Optional<Vocabulary> findById(String vocabularyId) {
            return Optional.ofNullable(data.get(vocabularyId));
        }

        @Override
        public List<Vocabulary> findByUserId(String userId) {
            return data.values().stream()
                    .filter(vocabulary -> vocabulary.userId().id().equals(userId))
                    .toList();
        }

        @Override
        public List<Vocabulary> findByIds(List<String> vocabularyIds) {
            return data.values().stream()
                    .filter(vocabulary -> vocabularyIds.contains(vocabulary.id().id()))
                    .toList();
        }

        @Override
        public Vocabulary replaceClozeSentence(String vocabularyId, String userId, Vocabulary vocabularyWithUpdatedCloze) {
            data.put(vocabularyId, vocabularyWithUpdatedCloze);
            return vocabularyWithUpdatedCloze;
        }
    }
}
