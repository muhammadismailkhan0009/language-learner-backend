package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.request.CreateReadingParagraphClozeSessionRequest;
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

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<ReadingParagraphClozeSessionResponse>>> listSessions(
            @RequestParam String userId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(service.listSessions(userId)));
    }

    @GetMapping("{sessionId}")
    public ResponseEntity<ApiResponse<ReadingParagraphClozeSessionResponse>> getSession(
            @PathVariable String sessionId,
            @RequestParam String userId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(service.getSession(userId, sessionId)));
    }

    @DeleteMapping("{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId, @RequestParam String userId) {
        service.deleteSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }
}
