package com.myriadcode.languagelearner.language_learning_system.application.controllers.study;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.study.request.CreateStudySessionRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.study.request.SubmitStudyAnswerRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.study.response.StudySessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.study.StudyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/study/sessions")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudySessionResponse>> createSession(@RequestBody CreateStudySessionRequest request) {
        var response = studyService.createSession(request.userId(), request.limit());
        return ResponseEntity.status(201).body(new ApiResponse<>(response));
    }

    @GetMapping("active")
    public ResponseEntity<ApiResponse<StudySessionResponse>> getActiveSession(@RequestParam String userId) {
        var response = studyService.getActiveSession(userId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @PostMapping("{sessionId}/items/{itemId}/answer")
    public ResponseEntity<ApiResponse<StudySessionResponse>> submitAnswer(@PathVariable String sessionId,
                                                                           @PathVariable String itemId,
                                                                           @RequestBody SubmitStudyAnswerRequest request) {
        var response = studyService.submitAnswer(sessionId, itemId, request.userId(), request.answer());
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
