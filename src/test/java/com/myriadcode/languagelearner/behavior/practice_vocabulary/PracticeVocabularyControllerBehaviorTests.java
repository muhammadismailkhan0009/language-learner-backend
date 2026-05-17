package com.myriadcode.languagelearner.behavior.practice_vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.PracticeVocabularyController;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PracticeVocabularyControllerBehaviorTests {

    @Test
    @DisplayName("extract API: delegates asynchronously and returns accepted status message")
    void extractDelegatesToService() throws Exception {
        var service = mock(PracticeVocabularyService.class);

        var controller = new PracticeVocabularyController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/practice-vocabulary/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1",
                                  "text": "text body"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.response.message").value("Extraction request is sent"));

        verify(service).enqueueExtraction("user-1", "text body");
    }
}
