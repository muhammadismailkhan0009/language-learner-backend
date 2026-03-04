package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.request.CreateReadingPracticeSessionRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/reading-practice/sessions")
public class ReadingPracticeController {

    private final ReadingPracticeService readingPracticeService;

    public ReadingPracticeController(ReadingPracticeService readingPracticeService) {
        this.readingPracticeService = readingPracticeService;
    }

    @PostMapping
    public ResponseEntity<Void> createSession(
            @RequestBody CreateReadingPracticeSessionRequest request
    ) {
        readingPracticeService.createSession(request.userId());
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadingPracticeSessionSummaryResponse>>> listSessions(
            @RequestParam String userId
    ) {
        var sessions = readingPracticeService.listSessions(userId);
        return ResponseEntity.ok(new ApiResponse<>(sessions));
    }

    @GetMapping("{sessionId}")
    public ResponseEntity<ApiResponse<ReadingPracticeSessionResponse>> getSession(
            @PathVariable String sessionId,
            @RequestParam String userId
    ) {
        var response = readingPracticeService.getSession(userId, sessionId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @DeleteMapping("{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String sessionId,
            @RequestParam String userId
    ) {
        readingPracticeService.deleteSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{sessionId}/flashcards/{flashcardId}")
    public ResponseEntity<Void> detachFlashcard(
            @PathVariable String sessionId,
            @PathVariable String flashcardId,
            @RequestParam String userId
    ) {
        readingPracticeService.detachFlashcard(userId, sessionId, flashcardId);
        return ResponseEntity.noContent().build();
    }
}
