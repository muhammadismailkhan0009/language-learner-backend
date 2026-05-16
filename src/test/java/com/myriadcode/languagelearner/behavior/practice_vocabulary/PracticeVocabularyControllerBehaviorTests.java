package com.myriadcode.languagelearner.behavior.practice_vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.PracticeVocabularyController;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.response.ExtractPracticeVocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PracticeVocabularyControllerBehaviorTests {

    @Test
    @DisplayName("extract API: delegates to service and returns extraction summary")
    void extractDelegatesToService() throws Exception {
        var service = mock(PracticeVocabularyService.class);
        when(service.extractAndStore("user-1", "text body"))
                .thenReturn(new ExtractPracticeVocabularyResponse(2, 1, List.of("gehen", "kennen"), List.of("v-1", "v-2")));

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.addedCount").value(2))
                .andExpect(jsonPath("$.response.existingCount").value(1))
                .andExpect(jsonPath("$.response.matchedWords[0]").value("gehen"))
                .andExpect(jsonPath("$.response.vocabularyIds[1]").value("v-2"));
    }
}
