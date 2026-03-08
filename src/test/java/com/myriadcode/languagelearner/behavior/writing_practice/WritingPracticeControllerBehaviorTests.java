package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.WritingPracticeController;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingSentencePairResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WritingPracticeControllerBehaviorTests {

    @Test
    @DisplayName("Create session API: returns 201 and forwards user id")
    void createSessionReturnsCreatedAndForwardsUserId() throws Exception {
        var service = mock(WritingPracticeService.class);
        var controller = new WritingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/writing-practice/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-1\"}"))
                .andExpect(status().isCreated());

        verify(service).createSession("user-1");
    }

    @Test
    @DisplayName("List sessions API: returns wrapped summaries for user")
    void listSessionsReturnsWrappedData() throws Exception {
        var service = mock(WritingPracticeService.class);
        var controller = new WritingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(service.listSessions("user-1")).thenReturn(List.of(
                new WritingPracticeSessionSummaryResponse("s-1", "Travel", Instant.parse("2026-01-01T00:00:00Z"), "Preview 1", 10, false),
                new WritingPracticeSessionSummaryResponse("s-2", "Work", Instant.parse("2026-01-02T00:00:00Z"), "Preview 2", 8, true)
        ));

        mockMvc.perform(get("/api/v1/writing-practice/sessions")
                        .queryParam("userId", "user-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(2))
                .andExpect(jsonPath("$.response[0].sessionId").value("s-1"))
                .andExpect(jsonPath("$.response[0].topic").value("Travel"))
                .andExpect(jsonPath("$.response[0].englishParagraphPreview").value("Preview 1"))
                .andExpect(jsonPath("$.response[0].vocabCount").value(10));

        verify(service).listSessions("user-1");
    }

    @Test
    @DisplayName("Get session API: returns wrapped session details")
    void getSessionReturnsWrappedSessionDetails() throws Exception {
        var service = mock(WritingPracticeService.class);
        var controller = new WritingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        var flashcard = new WritingVocabularyFlashCardView(
                "f-1",
                new WritingVocabularyFlashCardView.Front("word"),
                new WritingVocabularyFlashCardView.Back(
                        "Wort",
                        List.of(new WritingVocabularyFlashCardView.Sentence("ex-1", "Ich lerne.", "I learn."))
                ),
                true
        );

        when(service.getSession("user-1", "session-1")).thenReturn(new WritingPracticeSessionResponse(
                "session-1",
                "Travel",
                "English paragraph.",
                "Deutscher Absatz.",
                "My submitted answer",
                Instant.parse("2026-01-01T01:00:00Z"),
                List.of(new WritingSentencePairResponse("English sentence.", "Deutscher Satz.")),
                List.of(flashcard),
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        mockMvc.perform(get("/api/v1/writing-practice/sessions/{sessionId}", "session-1")
                        .queryParam("userId", "user-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.sessionId").value("session-1"))
                .andExpect(jsonPath("$.response.topic").value("Travel"))
                .andExpect(jsonPath("$.response.englishParagraph").value("English paragraph."))
                .andExpect(jsonPath("$.response.germanParagraph").value("Deutscher Absatz."))
                .andExpect(jsonPath("$.response.submittedAnswer").value("My submitted answer"))
                .andExpect(jsonPath("$.response.sentencePairs.length()").value(1))
                .andExpect(jsonPath("$.response.vocabFlashcards.length()").value(1));

        verify(service).getSession("user-1", "session-1");
    }

    @Test
    @DisplayName("Delete session API: returns 204 and forwards session and user ids")
    void deleteSessionReturnsNoContentAndForwardsArgs() throws Exception {
        var service = mock(WritingPracticeService.class);
        var controller = new WritingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(delete("/api/v1/writing-practice/sessions/{sessionId}", "session-1")
                        .queryParam("userId", "user-1"))
                .andExpect(status().isNoContent());

        verify(service).deleteSession("user-1", "session-1");
    }

    @Test
    @DisplayName("Detach flashcard API: returns 204 and forwards session/user/flashcard ids")
    void detachFlashcardReturnsNoContentAndForwardsArgs() throws Exception {
        var service = mock(WritingPracticeService.class);
        var controller = new WritingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(delete("/api/v1/writing-practice/sessions/{sessionId}/flashcards/{flashcardId}", "session-1", "flashcard-1")
                        .queryParam("userId", "user-1"))
                .andExpect(status().isNoContent());

        verify(service).detachFlashcard("user-1", "session-1", "flashcard-1");
    }

    @Test
    @DisplayName("Submit answer API: returns 200 and forwards user/session/answer")
    void submitAnswerReturnsOkAndForwardsArgs() throws Exception {
        var service = mock(WritingPracticeService.class);
        var controller = new WritingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/writing-practice/sessions/{sessionId}/submission", "session-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-1\",\"submittedAnswer\":\"My answer\"}"))
                .andExpect(status().isOk());

        verify(service).submitAnswer("user-1", "session-1", "My answer");
    }

    @Test
    @DisplayName("List sessions API: requires userId query parameter")
    void listSessionsRequiresUserIdQueryParam() throws Exception {
        var service = mock(WritingPracticeService.class);
        var controller = new WritingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/writing-practice/sessions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
