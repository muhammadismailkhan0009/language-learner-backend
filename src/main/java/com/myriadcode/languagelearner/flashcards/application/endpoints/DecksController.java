package com.myriadcode.languagelearner.flashcards.application.endpoints;

import com.myriadcode.languagelearner.flashcards.application.endpoints.dtos.ApiResponse;
import com.myriadcode.languagelearner.flashcards.domain.views.DeckView;
import com.myriadcode.languagelearner.flashcards.domain.views.FlashCardView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/decks")
public class DecksController {

    //    FIXME: for now, it is for language only. later when we generalize it, we'll add some identifier to decide from where to fetch cards data
    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<DeckView>>> getDecksData(
            @RequestParam FlashCardMode mode
    ) {

        System.out.println(mode.toString());
        var deck = List.of(new DeckView("id", "name", 2));

        return ResponseEntity.ok(new ApiResponse<>(deck));
    }

    @GetMapping("{deckId}/flashcards/v1")
    public ResponseEntity<ApiResponse<List<FlashCardView>>> getFlashCardsData(
            @PathVariable String deckId
    ) {

        var flashcards = List.of(
                new FlashCardView(deckId,
                        new FlashCardView.Front("text1"),
                        new FlashCardView.Back("text back 1"),
                        true,
                        null,
                        "scenario1"),
                new FlashCardView(deckId,
                        new FlashCardView.Front("text12"),
                        new FlashCardView.Back("text back 12"),
                        true,
                        null,
                        "scenario2")
        );


        return ResponseEntity.ok(new ApiResponse<>(flashcards));
    }


    //    data for endpoints
    public enum FlashCardMode {
        FRESH,
        REVIEW
    }

}
