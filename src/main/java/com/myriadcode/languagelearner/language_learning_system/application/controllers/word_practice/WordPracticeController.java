package com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice.request.CreateWordPracticeGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/word-practice")
public class WordPracticeController {
    private final WordPracticeGenerationRequestService generationRequestService;

    public WordPracticeController(WordPracticeGenerationRequestService generationRequestService) {
        this.generationRequestService = generationRequestService;
    }

    @PostMapping("generation")
    public ResponseEntity<ApiResponse<String>> requestGeneration(
            @RequestBody CreateWordPracticeGenerationRequest request
    ) {
        return ResponseEntity.accepted()
                .body(new ApiResponse<>(generationRequestService.request(request.userId())));
    }
}
