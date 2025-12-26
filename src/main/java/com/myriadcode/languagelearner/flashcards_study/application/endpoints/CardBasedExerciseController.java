package com.myriadcode.languagelearner.flashcards_study.application.endpoints;

import com.myriadcode.languagelearner.common.dtos.ApiRequest;
import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.common.enums.DeckInfo;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.flashcards_study.domain.views.FlashCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/exercise/audio-only")
public class CardBasedExerciseController {

    @Autowired
    private CardStudyService cardStudyService;

    @GetMapping("next/v1")
    public ResponseEntity<ApiResponse<Optional<FlashCardView>>> getNextCardToStudy(
            @RequestParam String userId) {

        var card = cardStudyService.getNextCardForRevision(DeckInfo.SENTENCES_REVISION, userId,3);
        return ResponseEntity.ok(new ApiResponse<>(card));
    }

    @PostMapping("{cardId}/review/v1")
    public ResponseEntity<Void> reviewStudiedCard(@PathVariable String cardId,
                                                  @RequestBody ApiRequest<CardsStudyController.CardRating, Void> rating) {
        cardStudyService.reviewStudiedCard(cardId, rating.payload().rating());
        return ResponseEntity.ok().build();

    }
}
