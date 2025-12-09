package com.myriadcode.languagelearner.flashcards_study.application.endpoints;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.flashcards_study.application.endpoints.dtos.ApiRequest;
import com.myriadcode.languagelearner.flashcards_study.application.endpoints.dtos.ApiResponse;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.flashcards_study.domain.views.FlashCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("api/decks/{deckId}/cards")
public class CardsStudyController {

    @Autowired
    private CardStudyService flashCardsService;

    @GetMapping("next/v1")
    public ResponseEntity<ApiResponse<Optional<FlashCardView>>> getNextCardToStudy(
            @PathVariable String deckId,
            @RequestParam String userId) {

        var card = flashCardsService.getNextCardToStudy(deckId, userId);
        return ResponseEntity.ok(new ApiResponse<>(card));
    }

    @PostMapping("{cardId}/review/v1")
    public ResponseEntity<Void> reviewStudiedCard(@PathVariable String cardId,
                                                  @RequestBody ApiRequest<CardRating, Void> rating) {
        flashCardsService.reviewStudiedCard(cardId, rating.payload().rating());
        return ResponseEntity.ok().build();

    }

    public record CardRating(Rating rating) {
    }
}
