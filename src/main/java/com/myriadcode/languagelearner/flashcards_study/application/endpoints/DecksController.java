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
/**
 * @deprecated Legacy deck-discovery controller for generic mixed-concern flow.
 * Prefer explicit module endpoints (for vocabulary: VocabularyCardsStudyController).
 * Remove after client migration to explicit endpoints.
 */
@Deprecated(since = "2026-02-22", forRemoval = true)
public class DecksController {

    /**
     * @deprecated Legacy mixed-concern endpoint.
     * Returns explicit private-vocabulary deck metadata to match current vocabulary flashcard flow.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<DeckView>>> getDecksData(
            @RequestParam(name = "mode") FlashCardMode mode,
            @RequestParam(name="userId") String userId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(toDecks(mode)));
    }

    /**
     * @deprecated Legacy mixed-concern endpoint.
     * Returns explicit private-vocabulary revision deck metadata to match current vocabulary flashcard flow.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    @GetMapping("/revision/v1")
    public ResponseEntity<ApiResponse<List<DeckView>>> getRevisionList(
            @RequestParam(name = "mode") FlashCardMode mode,
            @RequestParam(name="userId") String userId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(toDecks(mode)));
    }

    private List<DeckView> toDecks(FlashCardMode mode) {
        if (mode == FlashCardMode.REVISION) {
            return List.of(new DeckView(DeckInfo.PRIVATE_VOCABULARY_REVISION, "Private Vocabulary", 1));
        }
        return List.of(new DeckView(DeckInfo.PRIVATE_VOCABULARY, "Private Vocabulary", 1));
    }

    //    data for endpoints
    public enum FlashCardMode {
        FRESH,
        REVISION
    }

}
