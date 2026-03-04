package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.ReadingPracticeController;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeService;
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

class ReadingPracticeControllerBehaviorTests {

    @Test
    @DisplayName("Create session API: returns 201 and forwards user id")
    void createSessionReturnsCreatedAndForwardsUserId() throws Exception {
        var service = mock(ReadingPracticeService.class);
        var controller = new ReadingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/reading-practice/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-1\"}"))
                .andExpect(status().isCreated());

        verify(service).createSession("user-1");
    }

    @Test
    @DisplayName("List sessions API: returns wrapped summaries for user")
    void listSessionsReturnsWrappedData() throws Exception {
        var service = mock(ReadingPracticeService.class);
        var controller = new ReadingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(service.listSessions("user-1")).thenReturn(List.of(
                new ReadingPracticeSessionSummaryResponse(
                        "s-1",
                        "Travel",
                        Instant.parse("2026-01-01T00:00:00Z"),
                        "Preview 1",
                        10
                ),
                new ReadingPracticeSessionSummaryResponse(
                        "s-2",
                        "Work",
                        Instant.parse("2026-01-02T00:00:00Z"),
                        "Preview 2",
                        8
                )
        ));

        mockMvc.perform(get("/api/v1/reading-practice/sessions")
                        .queryParam("userId", "user-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(2))
                .andExpect(jsonPath("$.response[0].sessionId").value("s-1"))
                .andExpect(jsonPath("$.response[0].topic").value("Travel"))
                .andExpect(jsonPath("$.response[0].readingTextPreview").value("Preview 1"))
                .andExpect(jsonPath("$.response[0].vocabCount").value(10));

        verify(service).listSessions("user-1");
    }

    @Test
    @DisplayName("Get session API: returns wrapped session details")
    void getSessionReturnsWrappedSessionDetails() throws Exception {
        var service = mock(ReadingPracticeService.class);
        var controller = new ReadingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        var flashcard = new ReadingVocabularyFlashCardView(
                "f-1",
                new ReadingVocabularyFlashCardView.Front("Wort"),
                new ReadingVocabularyFlashCardView.Back(
                        "word",
                        List.of(new ReadingVocabularyFlashCardView.Sentence("ex-1", "Ich lerne.", "I learn."))
                ),
                false
        );

        when(service.getSession("user-1", "session-1")).thenReturn(new ReadingPracticeSessionResponse(
                "session-1",
                "Travel",
                "Generated reading",
                List.of(flashcard),
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        mockMvc.perform(get("/api/v1/reading-practice/sessions/{sessionId}", "session-1")
                        .queryParam("userId", "user-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.sessionId").value("session-1"))
                .andExpect(jsonPath("$.response.topic").value("Travel"))
                .andExpect(jsonPath("$.response.readingText").value("Generated reading"))
                .andExpect(jsonPath("$.response.vocabFlashcards.length()").value(1))
                .andExpect(jsonPath("$.response.vocabFlashcards[0].id").value("f-1"))
                .andExpect(jsonPath("$.response.vocabFlashcards[0].front.wordOrChunk").value("Wort"))
                .andExpect(jsonPath("$.response.vocabFlashcards[0].back.wordOrChunk").value("word"))
                .andExpect(jsonPath("$.response.vocabFlashcards[0].isReversed").value(false));

        verify(service).getSession("user-1", "session-1");
    }

    @Test
    @DisplayName("Delete session API: returns 204 and forwards session and user ids")
    void deleteSessionReturnsNoContentAndForwardsArgs() throws Exception {
        var service = mock(ReadingPracticeService.class);
        var controller = new ReadingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(delete("/api/v1/reading-practice/sessions/{sessionId}", "session-1")
                        .queryParam("userId", "user-1"))
                .andExpect(status().isNoContent());

        verify(service).deleteSession("user-1", "session-1");
    }

    @Test
    @DisplayName("List sessions API: requires userId query parameter")
    void listSessionsRequiresUserIdQueryParam() throws Exception {
        var service = mock(ReadingPracticeService.class);
        var controller = new ReadingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/reading-practice/sessions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Get session API: requires userId query parameter")
    void getSessionRequiresUserIdQueryParam() throws Exception {
        var service = mock(ReadingPracticeService.class);
        var controller = new ReadingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/reading-practice/sessions/{sessionId}", "session-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Delete session API: requires userId query parameter")
    void deleteSessionRequiresUserIdQueryParam() throws Exception {
        var service = mock(ReadingPracticeService.class);
        var controller = new ReadingPracticeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(delete("/api/v1/reading-practice/sessions/{sessionId}", "session-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
