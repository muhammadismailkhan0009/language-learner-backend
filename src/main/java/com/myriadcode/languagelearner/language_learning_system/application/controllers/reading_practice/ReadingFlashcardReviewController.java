package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.request.ReviewReadingFlashcardRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingFlashcardReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/reading-practice/sessions")
public class ReadingFlashcardReviewController {
    private final ReadingFlashcardReviewService reviewService;

    public ReadingFlashcardReviewController(ReadingFlashcardReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("flashcards/{flashcardId}/review")
    public ResponseEntity<Void> reviewFlashcard(
            @PathVariable String flashcardId,
            @RequestBody ReviewReadingFlashcardRequest request
    ) {
        reviewService.review(request.userId(), request.scenarioId(), flashcardId, request.rating());
        return ResponseEntity.ok().build();
    }
}
