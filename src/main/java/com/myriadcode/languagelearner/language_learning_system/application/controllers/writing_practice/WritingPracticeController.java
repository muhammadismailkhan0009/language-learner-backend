package com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.request.CreateWritingPracticeSessionRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.request.SubmitWritingPracticeAnswerRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
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
@RequestMapping("api/v1/writing-practice/sessions")
public class WritingPracticeController {

    private final WritingPracticeService writingPracticeService;

    public WritingPracticeController(WritingPracticeService writingPracticeService) {
        this.writingPracticeService = writingPracticeService;
    }

    @PostMapping
    public ResponseEntity<Void> createSession(@RequestBody CreateWritingPracticeSessionRequest request) {
        writingPracticeService.createSession(request.userId());
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WritingPracticeSessionSummaryResponse>>> listSessions(@RequestParam String userId) {
        return ResponseEntity.ok(new ApiResponse<>(writingPracticeService.listSessions(userId)));
    }

    @GetMapping("{sessionId}")
    public ResponseEntity<ApiResponse<WritingPracticeSessionResponse>> getSession(@PathVariable String sessionId,
                                                                                  @RequestParam String userId) {
        return ResponseEntity.ok(new ApiResponse<>(writingPracticeService.getSession(userId, sessionId)));
    }

    @PostMapping("{sessionId}/submission")
    public ResponseEntity<Void> submitAnswer(@PathVariable String sessionId,
                                             @RequestBody SubmitWritingPracticeAnswerRequest request) {
        writingPracticeService.submitAnswer(request.userId(), sessionId, request.submittedAnswer());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId,
                                              @RequestParam String userId) {
        writingPracticeService.deleteSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{sessionId}/flashcards/{flashcardId}")
    public ResponseEntity<Void> detachFlashcard(@PathVariable String sessionId,
                                                @PathVariable String flashcardId,
                                                @RequestParam String userId) {
        writingPracticeService.detachFlashcard(userId, sessionId, flashcardId);
        return ResponseEntity.noContent().build();
    }
}
