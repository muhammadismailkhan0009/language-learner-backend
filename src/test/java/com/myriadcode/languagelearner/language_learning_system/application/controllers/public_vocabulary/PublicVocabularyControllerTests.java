package com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.services.public_vocabulary.PublicVocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.repo.PublicVocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PublicVocabularyControllerTests {

    @Test
    @DisplayName("Publish public vocabulary API: accepts valid admin key and returns published payload")
    public void publishPublicVocabularyWithAdminKey() throws Exception {
        var vocabularyRepo = new FakeVocabularyRepo();
        var publicRepo = new FakePublicVocabularyRepo();
        vocabularyRepo.save(sampleVocabulary("vocab-1", "user-a"));

        var service = new PublicVocabularyOrchestrationService(publicRepo, vocabularyRepo);
        var controller = new PublicVocabularyController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/public-vocabularies/{vocabularyId}/v1", "vocab-1")
                        .queryParam("userId", "user-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adminKey\":\"112233\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.sourceVocabularyId").value("vocab-1"))
                .andExpect(jsonPath("$.response.publishedByUserId").value("user-a"))
                .andExpect(jsonPath("$.response.surface").value("gehen"));
    }

    @Test
    @DisplayName("Fetch public vocabularies API: returns only published items")
    public void fetchPublicVocabularyReturnsPublishedItems() throws Exception {
        var vocabularyRepo = new FakeVocabularyRepo();
        var publicRepo = new FakePublicVocabularyRepo();
        vocabularyRepo.save(sampleVocabulary("vocab-1", "user-a"));
        publicRepo.save(new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId("pub-1"),
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.now()
        ));

        var service = new PublicVocabularyOrchestrationService(publicRepo, vocabularyRepo);
        var controller = new PublicVocabularyController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/public-vocabularies/v1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(1))
                .andExpect(jsonPath("$.response[0].sourceVocabularyId").value("vocab-1"))
                .andExpect(jsonPath("$.response[0].surface").value("gehen"));
    }

    @Test
    @DisplayName("Add public vocabulary to private: copies data without public reference")
    public void addPublicVocabularyToPrivateCreatesCopy() throws Exception {
        var vocabularyRepo = new FakeVocabularyRepo();
        var publicRepo = new FakePublicVocabularyRepo();
        vocabularyRepo.save(sampleVocabulary("vocab-7", "user-a"));
        publicRepo.save(new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId("pub-7"),
                new Vocabulary.VocabularyId("vocab-7"),
                new UserId("user-a"),
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.now()
        ));

        var service = new PublicVocabularyOrchestrationService(publicRepo, vocabularyRepo);
        var controller = new PublicVocabularyController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/public-vocabularies/{publicVocabularyId}/private/v1", "pub-7")
                        .queryParam("userId", "user-b")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.userId").value("user-b"))
                .andExpect(jsonPath("$.response.surface").value("gehen"))
                .andExpect(jsonPath("$.response.translation").value("to go"))
                .andExpect(jsonPath("$.response.id").isNotEmpty())
                .andExpect(jsonPath("$.response.id").value(org.hamcrest.Matchers.not("vocab-7")));
    }

    @Test
    @DisplayName("Add public vocabulary to private: skips when matching private vocab surface exists")
    public void addPublicVocabularyToPrivateSkipsExisting() throws Exception {
        var vocabularyRepo = new FakeVocabularyRepo();
        var publicRepo = new FakePublicVocabularyRepo();
        vocabularyRepo.save(sampleVocabulary("vocab-9", "user-a"));
        vocabularyRepo.save(sampleVocabulary("private-9", "user-b"));
        publicRepo.save(new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId("pub-9"),
                new Vocabulary.VocabularyId("vocab-9"),
                new UserId("user-a"),
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.now()
        ));

        var service = new PublicVocabularyOrchestrationService(publicRepo, vocabularyRepo);
        var controller = new PublicVocabularyController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/public-vocabularies/{publicVocabularyId}/private/v1", "pub-9")
                        .queryParam("userId", "user-b")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.id").value("private-9"))
                .andExpect(jsonPath("$.response.userId").value("user-b"))
                .andExpect(jsonPath("$.response.surface").value("gehen"))
                .andExpect(jsonPath("$.response.translation").value("to go"));
    }

    private Vocabulary sampleVocabulary(String id, String userId) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(id),
                new UserId(userId),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                "base note",
                List.of(new VocabularyExampleSentence(
                        new VocabularyExampleSentence.VocabularyExampleSentenceId("ex-1"),
                        "Ich gehe nach Hause.",
                        "I go home."
                )),
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
            var vocabulary = data.get(vocabularyId);
            if (vocabulary == null || !vocabulary.userId().id().equals(userId)) {
                return Optional.empty();
            }
            return Optional.of(vocabulary);
        }

        @Override
        public Optional<Vocabulary> findById(String vocabularyId) {
            return Optional.ofNullable(data.get(vocabularyId));
        }

        @Override
        public List<Vocabulary> findByUserId(String userId) {
            return data.values().stream().filter(v -> v.userId().id().equals(userId)).toList();
        }

        @Override
        public List<Vocabulary> findByIds(List<String> vocabularyIds) {
            return data.values().stream().filter(v -> vocabularyIds.contains(v.id().id())).toList();
        }
    }

    private static class FakePublicVocabularyRepo implements PublicVocabularyRepo {

        private final Map<String, PublicVocabulary> dataBySourceVocabularyId = new HashMap<>();
        private final Map<String, PublicVocabulary> dataById = new HashMap<>();

        @Override
        public PublicVocabulary save(PublicVocabulary publicVocabulary) {
            var toStore = publicVocabulary.id() == null
                    ? new PublicVocabulary(
                    new PublicVocabulary.PublicVocabularyId(UUID.randomUUID().toString()),
                    publicVocabulary.sourceVocabularyId(),
                    publicVocabulary.publishedByUserId(),
                    publicVocabulary.status(),
                    publicVocabulary.publishedAt()
            ) : publicVocabulary;
            dataBySourceVocabularyId.put(toStore.sourceVocabularyId().id(), toStore);
            dataById.put(toStore.id().id(), toStore);
            return toStore;
        }

        @Override
        public Optional<PublicVocabulary> findBySourceVocabularyId(String sourceVocabularyId) {
            return Optional.ofNullable(dataBySourceVocabularyId.get(sourceVocabularyId));
        }

        @Override
        public Optional<PublicVocabulary> findById(String publicVocabularyId) {
            return Optional.ofNullable(dataById.get(publicVocabularyId));
        }

        @Override
        public List<PublicVocabulary> findAllByStatus(PublicVocabulary.PublicVocabularyStatus status) {
            return dataBySourceVocabularyId.values().stream()
                    .filter(v -> v.status() == status)
                    .toList();
        }
    }
}
