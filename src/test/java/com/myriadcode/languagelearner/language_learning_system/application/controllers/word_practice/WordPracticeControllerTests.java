package com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice;

import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationRequestService;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeAnswerService;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeAnswerResult;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeQueryService;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class WordPracticeControllerTests {

    @Test
    void generationRequestCreatesPendingMcpJob() throws Exception {
        var requestService = new StubRequestService();
        var mvc = MockMvcBuilders.standaloneSetup(new WordPracticeController(requestService, null, null)).build();

        mvc.perform(post("/api/v1/word-practice/generation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"user-1"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.response")
                        .value("Word Practice generation requested. Run your MCP tool."));
    }

    @Test
    void listsQueueWithVocabularyCapacityMetadata() throws Exception {
        var queryService = new StubQueryService();
        var mvc = MockMvcBuilders.standaloneSetup(new WordPracticeController(null, queryService, null)).build();

        mvc.perform(get("/api/v1/word-practice").param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.activeVocabularyCount").value(1))
                .andExpect(jsonPath("$.response.maximumVocabularyCount").value(15))
                .andExpect(jsonPath("$.response.generationVocabularyCount").value(10))
                .andExpect(jsonPath("$.response.practices[0].id").value("practice-1"));
    }

    @Test
    void submitsAnAnswer() throws Exception {
        var answerService = new StubAnswerService();
        var mvc = MockMvcBuilders.standaloneSetup(new WordPracticeController(null, null, answerService)).build();

        mvc.perform(post("/api/v1/word-practice/practice-1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"user-1","answer":"house"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.correct").value(true))
                .andExpect(jsonPath("$.response.vocabularySurface").value("sich bei jemandem melden"));
    }

    private static final class StubRequestService extends WordPracticeGenerationRequestService {
        private StubRequestService() {
            super(null, null);
        }

        @Override
        public String request(String userId) {
            return "Word Practice generation requested. Run your MCP tool.";
        }
    }

    private static final class StubQueryService extends WordPracticeQueryService {
        private StubQueryService() {
            super(null);
        }

        @Override
        public List<WordPractice> findAll(String userId) {
            return List.of(new WordPractice(
                    "practice-1", userId, "vocabulary-1", WordPracticeDirection.GERMAN_TO_ENGLISH,
                    "Das Haus ist groß.", "Das ___ ist groß.", "Das Haus ist groß.", "house",
                    List.of("home"), Instant.parse("2026-09-08T00:00:00Z")
            ));
        }
    }

    private static final class StubAnswerService extends WordPracticeAnswerService {
        private StubAnswerService() {
            super(null, null, null, null);
        }

        @Override
        public WordPracticeAnswerResult submit(String userId, String practiceId, String answer) {
            return new WordPracticeAnswerResult(true, "sich bei jemandem melden");
        }
    }
}
