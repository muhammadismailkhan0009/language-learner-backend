package com.myriadcode.languagelearner.behavior.reading_paragraph_cloze;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.ReadingParagraphClozeController;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.ReadingParagraphClozeService;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeCard;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.application.externals.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReadingParagraphClozeControllerBehaviorTests {

    @Test
    @DisplayName("Create session API: returns 201 and forwards user and limit")
    void createSessionReturnsCreated() throws Exception {
        var service = buildServiceForCreate();
        var controller = new ReadingParagraphClozeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/reading-cloze-paragraph/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-1\",\"limit\":50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response.topic").value("Travel | Work"))
                .andExpect(jsonPath("$.response.cards.length()").value(2));
    }

    @Test
    @DisplayName("Get active session API: returns wrapped response")
    void getActiveSessionReturnsResponse() throws Exception {
        var service = buildServiceForActiveSession();
        var controller = new ReadingParagraphClozeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/reading-cloze-paragraph/sessions/active")
                        .queryParam("userId", "user-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("ACTIVE"))
                .andExpect(jsonPath("$.response.cards.length()").value(1));
    }

    @Test
    @DisplayName("Rate card API: returns 200 and forwards payload")
    void rateCardReturnsOk() throws Exception {
        var service = buildServiceForRate();
        var controller = new ReadingParagraphClozeController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/reading-cloze-paragraph/sessions/{sessionId}/ratings", "s-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-1\",\"flashcardId\":\"f-1\",\"rating\":\"GOOD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.sessionId").value("s-1"));
    }

    private ReadingParagraphClozeService buildServiceForCreate() {
        var repo = mock(ReadingParagraphClozeRepo.class);
        var flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
        var vocabularyRepo = mock(VocabularyRepo.class);
        var recentReadingTopicsApi = mock(FetchRecentReadingTopicsApi.class);
        var recentWritingTopicsApi = mock(FetchRecentWritingTopicsApi.class);
        var readingPracticeLlmApi = mock(ReadingPracticeLlmApi.class);
        var reviewVocabularyFlashcardApi = mock(ReviewVocabularyFlashcardApi.class);

        when(repo.findLatestByUserId("user-1")).thenReturn(Optional.empty());
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true),
                new VocabularyFlashcardReviewRecord("f-2", "v-2", State.NEW, true)
        ));
        when(vocabularyRepo.findByIds(List.of("v-1", "v-2"))).thenReturn(List.of(
                vocab("v-1", "gehen", "to go"),
                vocab("v-2", "kennen", "to know")
        ));
        when(recentReadingTopicsApi.findRecentTopics("user-1", 3)).thenReturn(List.of("Travel"));
        when(recentWritingTopicsApi.findRecentTopics("user-1", 3)).thenReturn(List.of("Work"));
        when(readingPracticeLlmApi.generateReadingParagraphCloze(eq("Travel | Work"), any(), eq("B1")))
                .thenReturn(new ReadingParagraphClozeGeneration(
                        "Ich ___ nach Berlin und ___ Anna.",
                        List.of(
                                new ReadingParagraphClozeGeneration.Item("gehen", "go", List.of("gehe"), "___"),
                                new ReadingParagraphClozeGeneration.Item("kennen", "know", List.of("kenne"), "___")
                        )
                ));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        return new ReadingParagraphClozeService(repo, flashcardReviewsApi, vocabularyRepo, recentReadingTopicsApi,
                recentWritingTopicsApi, readingPracticeLlmApi, reviewVocabularyFlashcardApi);
    }

    private ReadingParagraphClozeService buildServiceForActiveSession() {
        var repo = mock(ReadingParagraphClozeRepo.class);
        var flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
        var vocabularyRepo = mock(VocabularyRepo.class);
        var recentReadingTopicsApi = mock(FetchRecentReadingTopicsApi.class);
        var recentWritingTopicsApi = mock(FetchRecentWritingTopicsApi.class);
        var readingPracticeLlmApi = mock(ReadingPracticeLlmApi.class);
        var reviewVocabularyFlashcardApi = mock(ReviewVocabularyFlashcardApi.class);

        when(repo.findLatestByUserId("user-1")).thenReturn(Optional.of(new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-1"),
                new UserId("user-1"),
                "topic",
                "Ich ___.",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new ReadingParagraphClozeCard(
                        new ReadingParagraphClozeCard.ReadingParagraphClozeCardId("c-1"),
                        "f-1",
                        "v-1",
                        Instant.parse("2026-01-01T00:00:01Z")
                ))
        )));
        when(vocabularyRepo.findByIds(List.of("v-1"))).thenReturn(List.of(vocab("v-1", "gehen", "to go")));
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.NEW, true)
        ));

        return new ReadingParagraphClozeService(repo, flashcardReviewsApi, vocabularyRepo, recentReadingTopicsApi,
                recentWritingTopicsApi, readingPracticeLlmApi, reviewVocabularyFlashcardApi);
    }

    private ReadingParagraphClozeService buildServiceForRate() {
        var repo = mock(ReadingParagraphClozeRepo.class);
        var flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
        var vocabularyRepo = mock(VocabularyRepo.class);
        var recentReadingTopicsApi = mock(FetchRecentReadingTopicsApi.class);
        var recentWritingTopicsApi = mock(FetchRecentWritingTopicsApi.class);
        var readingPracticeLlmApi = mock(ReadingPracticeLlmApi.class);
        var reviewVocabularyFlashcardApi = mock(ReviewVocabularyFlashcardApi.class);

        var session = new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-1"),
                new UserId("user-1"),
                "topic",
                "Ich ___.",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new ReadingParagraphClozeCard(
                        new ReadingParagraphClozeCard.ReadingParagraphClozeCardId("c-1"),
                        "f-1",
                        "v-1",
                        Instant.parse("2026-01-01T00:00:01Z")
                ))
        );
        when(repo.findByIdAndUserId("s-1", "user-1")).thenReturn(Optional.of(session), Optional.of(session));
        when(vocabularyRepo.findByIds(List.of("v-1"))).thenReturn(List.of(vocab("v-1", "gehen", "to go")));
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.NEW, true)
        ));

        return new ReadingParagraphClozeService(repo, flashcardReviewsApi, vocabularyRepo, recentReadingTopicsApi,
                recentWritingTopicsApi, readingPracticeLlmApi, reviewVocabularyFlashcardApi);
    }

    private Vocabulary vocab(String id, String surface, String translation) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(id),
                new UserId("user-1"),
                surface,
                translation,
                Vocabulary.EntryKind.WORD,
                null,
                List.of(),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
