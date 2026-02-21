package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        var controller = new VocabularyController(service);
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
        var controller = new VocabularyController(service);
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
        var controller = new VocabularyController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/vocabularies/v1")
                        .queryParam("userId", "user-a")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(1))
                .andExpect(jsonPath("$.response[0].id").value("vocab-1"))
                .andExpect(jsonPath("$.response[0].userId").value("user-a"));
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
                )
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
        public List<Vocabulary> findByUserId(String userId) {
            return data.values().stream()
                    .filter(vocabulary -> vocabulary.userId().id().equals(userId))
                    .toList();
        }
    }
}
