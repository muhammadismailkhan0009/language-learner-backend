package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.request.CreateReadingParagraphClozeSessionRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.request.RateReadingParagraphClozeCardRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.ReadingParagraphClozeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/reading-cloze-paragraph/sessions")
public class ReadingParagraphClozeController {

    private final ReadingParagraphClozeService service;

    public ReadingParagraphClozeController(ReadingParagraphClozeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReadingParagraphClozeSessionResponse>> createSession(
            @RequestBody CreateReadingParagraphClozeSessionRequest request
    ) {
        var response = service.createSession(request.userId(), request.limit());
        return ResponseEntity.status(201).body(new ApiResponse<>(response));
    }

    @GetMapping("active")
    public ResponseEntity<ApiResponse<ReadingParagraphClozeSessionResponse>> getActiveSession(
            @RequestParam String userId
    ) {
        var response = service.getActiveSession(userId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @PostMapping("{sessionId}/ratings")
    public ResponseEntity<ApiResponse<ReadingParagraphClozeSessionResponse>> rateCard(
            @PathVariable String sessionId,
            @RequestBody RateReadingParagraphClozeCardRequest request
    ) {
        var response = service.rateCard(sessionId, request.userId(), request.flashcardId(), request.rating());
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
