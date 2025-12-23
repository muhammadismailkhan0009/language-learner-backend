package com.myriadcode.languagelearner.flashcards_study.application.endpoints;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.common.enums.DeckInfo;
import com.myriadcode.languagelearner.flashcards_study.application.endpoints.dtos.ApiRequest;
import com.myriadcode.languagelearner.flashcards_study.application.endpoints.dtos.ApiResponse;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.flashcards_study.domain.views.FlashCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("api/decks/{deckId}/cards")
public class CardsStudyController {

    @Autowired
    private CardStudyService flashCardsService;

    @GetMapping("next/v1")
    public ResponseEntity<ApiResponse<List<FlashCardView>>> getNextCardToStudy(
            @PathVariable DeckInfo deckId,
            @RequestParam String userId) {

        List<FlashCardView> card = new ArrayList<>();
        if(DeckInfo.SENTENCES.equals(deckId)) {
             card = flashCardsService.getNextCardsToStudy(deckId, userId,3);
        }
        else if(DeckInfo.SENTENCES_REVISION.equals(deckId)) {
            card = flashCardsService.getCardsForRevision(deckId, userId,1);
        }
        return ResponseEntity.ok(new ApiResponse<>(card));
    }

    @GetMapping("/revision/next/v1")
    public ResponseEntity<ApiResponse<Optional<FlashCardView>>> getNextCardToRevise(
            @PathVariable DeckInfo deckId,
            @RequestParam String userId) {

        var card = flashCardsService.getNextCardForRevision(deckId, userId);
        return ResponseEntity.ok(new ApiResponse<>(card));
    }

//    FIXME: we are not using it currently. may be later. but not right now.
    @PostMapping("{cardId}/review/v1")
    public ResponseEntity<Void> reviewStudiedCard(@PathVariable String cardId,
                                                  @RequestBody ApiRequest<CardRating, Void> rating) {
        flashCardsService.reviewStudiedCard(cardId, rating.payload().rating());
        return ResponseEntity.ok().build();

    }

    public record CardRating(Rating rating) {
    }
}
