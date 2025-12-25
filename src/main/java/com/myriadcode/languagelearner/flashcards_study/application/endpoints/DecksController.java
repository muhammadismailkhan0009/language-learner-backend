package com.myriadcode.languagelearner.flashcards_study.application.endpoints;

import com.myriadcode.languagelearner.common.enums.DeckInfo;
import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.flashcards_study.domain.views.DeckView;
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
            @RequestParam(name = "mode") FlashCardMode mode,
            @RequestParam(name="userId") String userId
    ) {

        var deck = List.of(
                new DeckView(DeckInfo.SENTENCES, "Sentences", 1));

        return ResponseEntity.ok(new ApiResponse<>(deck));
    }

    @GetMapping("/revision/v1")
    public ResponseEntity<ApiResponse<List<DeckView>>> getRevisionList(
            @RequestParam(name = "mode") FlashCardMode mode,
            @RequestParam(name="userId") String userId
    ) {

        var deck = List.of(
                new DeckView(DeckInfo.SENTENCES_REVISION, "Sentences", 1));

        return ResponseEntity.ok(new ApiResponse<>(deck));
    }

    //    data for endpoints
    public enum FlashCardMode {
        FRESH,
        REVISION
    }

}
