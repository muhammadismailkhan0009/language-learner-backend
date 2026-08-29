package com.myriadcode.languagelearner.behavior.vocabulary_extraction;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.VocabularyController;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionRequestRepo;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VocabularyExtractionControllerContractTests {

    @Test
    void acceptsGenericVocabularyExtractionRequest() throws Exception {
        var requests = new RequestRepo();
        var controller = new VocabularyController(null, null, service(requests));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/v1/vocabularies/extractions/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1",
                                  "sourceText": "Lina waits for the train."
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.response.requestId").isNotEmpty())
                .andExpect(jsonPath("$.response.message").value("Vocabulary extraction requested. Run your MCP tool."));

        assertThat(requests.values).hasSize(1);
        assertThat(requests.values.values().iterator().next().sourceText())
                .isEqualTo("Lina waits for the train.");
    }

    @Test
    void rejectsBlankSourceBeforeCallingApplicationService() throws Exception {
        var requests = new RequestRepo();
        var controller = new VocabularyController(null, null, service(requests));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/v1/vocabularies/extractions/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": "user-1", "sourceText": "   "}
                                """))
                .andExpect(status().isBadRequest());

        assertThat(requests.values).isEmpty();
    }

    private VocabularyExtractionService service(RequestRepo requests) {
        return new VocabularyExtractionService(requests, null, null,
                new ContentGenerationJobService(new JobRepo()), null, null);
    }

    private static final class RequestRepo implements VocabularyExtractionRequestRepo {
        private final Map<String, VocabularyExtractionRequest> values = new LinkedHashMap<>();
        public VocabularyExtractionRequest save(VocabularyExtractionRequest request) { values.put(request.id().id(), request); return request; }
        public Optional<VocabularyExtractionRequest> findOldestByUserId(String userId) { return values.values().stream().findFirst(); }
        public Optional<VocabularyExtractionRequest> findByIdAndUserId(String requestId, String userId) { return Optional.ofNullable(values.get(requestId)); }
        public boolean existsByUserId(String userId) { return !values.isEmpty(); }
        public void delete(VocabularyExtractionRequest.VocabularyExtractionRequestId id) { values.remove(id.id()); }
    }

    private static final class JobRepo implements ContentGenerationJobRepo {
        private final Map<String, ContentGenerationJob> values = new LinkedHashMap<>();
        public ContentGenerationJob save(ContentGenerationJob job) { values.put(key(job.userId(), job.type()), job); return job; }
        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) { return Optional.ofNullable(values.get(key(userId, type))); }
        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) { values.remove(key(userId, type)); }
        private String key(String userId, ContentGenerationJobType type) { return userId + ":" + type; }
    }
}
