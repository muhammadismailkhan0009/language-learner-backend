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
    public ResponseEntity<ApiResponse<String>> createSession(@RequestBody CreateWritingPracticeSessionRequest request) {
        writingPracticeService.createSessionReactive(request.userId());
        return ResponseEntity.accepted().body(new ApiResponse<>("Creation in progress. Will display when done."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WritingPracticeSessionSummaryResponse>>> listSessions(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(new ApiResponse<>(writingPracticeService.listSessions(userId)));
    }

    @GetMapping("{sessionId}")
    public ResponseEntity<ApiResponse<WritingPracticeSessionResponse>> getSession(@PathVariable("sessionId") String sessionId,
                                                                                  @RequestParam("userId") String userId) {
        return ResponseEntity.ok(new ApiResponse<>(writingPracticeService.getSession(userId, sessionId)));
    }

    @PostMapping("{sessionId}/submission")
    public ResponseEntity<Void> submitAnswer(@PathVariable("sessionId") String sessionId,
                                             @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                             @RequestBody SubmitWritingPracticeAnswerRequest request) {
        writingPracticeService.submitAnswer(request.userId(), sessionId, request.submittedAnswer(), draft);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{sessionId}/feedback/re-evaluate")
    public ResponseEntity<ApiResponse<WritingPracticeSessionResponse>> reEvaluateFeedback(@PathVariable("sessionId") String sessionId,
                                                                                         @RequestParam("userId") String userId) {
        return ResponseEntity.ok(new ApiResponse<>(writingPracticeService.reEvaluateFeedback(userId, sessionId)));
    }

    @DeleteMapping("{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable("sessionId") String sessionId,
                                              @RequestParam("userId") String userId) {
        writingPracticeService.deleteSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{sessionId}/flashcards/{flashcardId}")
    public ResponseEntity<Void> detachFlashcard(@PathVariable("sessionId") String sessionId,
                                                @PathVariable("flashcardId") String flashcardId,
                                                @RequestParam("userId") String userId) {
        writingPracticeService.detachFlashcard(userId, sessionId, flashcardId);
        return ResponseEntity.noContent().build();
    }
}
