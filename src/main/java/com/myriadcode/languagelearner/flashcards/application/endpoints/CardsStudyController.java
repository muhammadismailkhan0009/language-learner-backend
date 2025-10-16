package com.myriadcode.languagelearner.flashcards.application.endpoints;

import com.myriadcode.languagelearner.flashcards.application.endpoints.dtos.ApiRequest;
import com.myriadcode.languagelearner.flashcards.application.endpoints.dtos.ApiResponse;
import com.myriadcode.languagelearner.flashcards.application.services.CardStudyService;
import com.myriadcode.languagelearner.flashcards.domain.views.FlashCardView;
import io.github.openspacedrepetition.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/decks/{deckId}/cards")
public class CardsStudyController {

    @Autowired
    private CardStudyService flashCardsService;

    @PostMapping("next")
    public ResponseEntity<ApiResponse<Optional<FlashCardView>>> getNextCardToStudy(@PathVariable String deckId) {
//        get next card to study from cache
        var card = flashCardsService.getNextCardToStudy(deckId);
        return ResponseEntity.ok(new ApiResponse<>(card));
    }

    @PostMapping("{cardId}/review")
    public ResponseEntity<Void> reviewStudiedCard(@PathVariable String cardId,
                                                  @RequestBody ApiRequest<CardRating> rating) {
        flashCardsService.reviewStudiedCard(cardId, rating.payload().rating());
        return ResponseEntity.ok().build();

    }

    public record CardRating(Rating rating) {
    }
}
