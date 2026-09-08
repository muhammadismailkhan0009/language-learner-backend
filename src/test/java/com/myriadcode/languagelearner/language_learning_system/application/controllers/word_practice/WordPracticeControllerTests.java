package com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice;

import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class WordPracticeControllerTests {

    @Test
    void generationRequestCreatesPendingMcpJob() throws Exception {
        var requestService = new StubRequestService();
        var mvc = MockMvcBuilders.standaloneSetup(new WordPracticeController(requestService)).build();

        mvc.perform(post("/api/v1/word-practice/generation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"user-1"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.response")
                        .value("Word Practice generation requested. Run your MCP tool."));
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
}
