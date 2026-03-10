package com.myriadcode.languagelearner.flashcards_study.application.endpoints;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.flashcards_study.domain.views.VocabularyFlashCardView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Vocabulary Flashcards Controller")
class VocabularyCardsStudyControllerTests {

    private CardStudyService cardStudyService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cardStudyService = Mockito.mock(CardStudyService.class);
        var controller = new VocabularyCardsStudyController(cardStudyService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Next cards endpoint: fetches private vocabulary study cards only")
    void nextCardsEndpointReturnsVocabularyStudyCards() throws Exception {
        when(cardStudyService.getNextPrivateVocabularyCardsToStudy(eq("user-1"), eq(1)))
                .thenReturn(List.of(new VocabularyFlashCardView(
                        "card-1",
                        new VocabularyFlashCardView.VocabularyFlashCardFront("Ich ___ Deutsch.", "learn"),
                        new VocabularyFlashCardView.VocabularyFlashCardBack(List.of("lerne"), "lerne", "learn", "verb notes"),
                        true,
                        false
                )));

        mockMvc.perform(get("/api/v1/vocabulary-flashcards/cards/next/v1")
                        .queryParam("userId", "user-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(1))
                .andExpect(jsonPath("$.response[0].id").value("card-1"))
                .andExpect(jsonPath("$.response[0].front.clozeText").value("Ich ___ Deutsch."))
                .andExpect(jsonPath("$.response[0].front.hint").value("learn"))
                .andExpect(jsonPath("$.response[0].back.answerWords.length()").value(1))
                .andExpect(jsonPath("$.response[0].back.answerWords[0]").value("lerne"))
                .andExpect(jsonPath("$.response[0].back.answerText").value("lerne"))
                .andExpect(jsonPath("$.response[0].back.answerTranslation").value("learn"))
                .andExpect(jsonPath("$.response[0].back.notes").value("verb notes"));

        verify(cardStudyService).getNextPrivateVocabularyCardsToStudy("user-1", 1);
    }

    @Test
    @DisplayName("Revision next endpoint: fetches one private vocabulary revision card")
    void revisionNextEndpointReturnsVocabularyRevisionCard() throws Exception {
        when(cardStudyService.getNextPrivateVocabularyCardForRevision(eq("user-2"), eq(1)))
                .thenReturn(Optional.of(new VocabularyFlashCardView(
                        "card-2",
                        new VocabularyFlashCardView.VocabularyFlashCardFront("Ich ___ Deutsch.", "learn"),
                        new VocabularyFlashCardView.VocabularyFlashCardBack(List.of("lerne"), "lerne", "learn", "verb notes"),
                        true,
                        true
                )));

        mockMvc.perform(get("/api/v1/vocabulary-flashcards/cards/revision/next/v1")
                        .queryParam("userId", "user-2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.id").value("card-2"));

        verify(cardStudyService).getNextPrivateVocabularyCardForRevision("user-2", 1);
    }

    @Test
    @DisplayName("Revision list endpoint: supports explicit count for private vocabulary revision")
    void revisionListEndpointReturnsVocabularyRevisionCards() throws Exception {
        when(cardStudyService.getPrivateVocabularyCardsForRevision(eq("user-3"), eq(2)))
                .thenReturn(List.of(
                        new VocabularyFlashCardView(
                                "card-3",
                                new VocabularyFlashCardView.VocabularyFlashCardFront("Ich ___ nach Hause.", "go"),
                                new VocabularyFlashCardView.VocabularyFlashCardBack(List.of("gehe"), "gehe", "go", null),
                                true,
                                true
                        ),
                        new VocabularyFlashCardView(
                                "card-4",
                                new VocabularyFlashCardView.VocabularyFlashCardFront("Wir ___ morgen.", "come"),
                                new VocabularyFlashCardView.VocabularyFlashCardBack(List.of("kommen"), "kommen", "come", "aux notes"),
                                true,
                                true
                        )
                ));

        mockMvc.perform(get("/api/v1/vocabulary-flashcards/cards/revision/v1")
                        .queryParam("userId", "user-3")
                        .queryParam("count", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.length()").value(2));

        verify(cardStudyService).getPrivateVocabularyCardsForRevision("user-3", 2);
    }

    @Test
    @DisplayName("Review endpoint: forwards rating for private vocabulary card review")
    void reviewEndpointDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/v1/vocabulary-flashcards/cards/{cardId}/review/v1", "card-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "payload": {
                                    "rating": "GOOD"
                                  },
                                  "additionalData": null
                                }
                                """))
                .andExpect(status().isOk());

        verify(cardStudyService).reviewVocabularyStudiedCard("card-9", Rating.GOOD);
    }
}
