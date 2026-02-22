package com.myriadcode.languagelearner.flashcards_study.application.endpoints;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.common.dtos.ApiRequest;
import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.flashcards_study.domain.views.VocabularyFlashCardView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/vocabulary-flashcards/cards")
public class VocabularyCardsStudyController {

    private final CardStudyService cardStudyService;

    public VocabularyCardsStudyController(CardStudyService cardStudyService) {
        this.cardStudyService = cardStudyService;
    }

    @GetMapping("next/v1")
    public ResponseEntity<ApiResponse<List<VocabularyFlashCardView>>> getNextCardsToStudy(
            @RequestParam String userId
    ) {
        var cards = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 1);
        return ResponseEntity.ok(new ApiResponse<>(cards));
    }

    @GetMapping("revision/next/v1")
    public ResponseEntity<ApiResponse<Optional<VocabularyFlashCardView>>> getNextCardForRevision(
            @RequestParam String userId
    ) {
        var card = cardStudyService.getNextPrivateVocabularyCardForRevision(userId, 1);
        return ResponseEntity.ok(new ApiResponse<>(card));
    }

    @GetMapping("revision/v1")
    public ResponseEntity<ApiResponse<List<VocabularyFlashCardView>>> getCardsForRevision(
            @RequestParam String userId,
            @RequestParam(defaultValue = "1") int count
    ) {
        var cards = cardStudyService.getPrivateVocabularyCardsForRevision(userId, count);
        return ResponseEntity.ok(new ApiResponse<>(cards));
    }

    @PostMapping("{cardId}/review/v1")
    public ResponseEntity<Void> reviewStudiedCard(
            @PathVariable String cardId,
            @RequestBody ApiRequest<CardRating, Void> rating
    ) {
        cardStudyService.reviewVocabularyStudiedCard(cardId, rating.payload().rating());
        return ResponseEntity.ok().build();
    }

    public record CardRating(Rating rating) {
    }
}
