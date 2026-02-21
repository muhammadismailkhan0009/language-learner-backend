package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/vocabularies")
public class VocabularyController {

    private final VocabularyOrchestrationService vocabularyOrchestrationService;

    public VocabularyController(VocabularyOrchestrationService vocabularyOrchestrationService) {
        this.vocabularyOrchestrationService = vocabularyOrchestrationService;
    }

    @PostMapping("v1")
    public ResponseEntity<ApiResponse<VocabularyResponse>> addVocabulary(
            @RequestParam String userId,
            @RequestBody AddVocabularyRequest request
    ) {
        var response = vocabularyOrchestrationService.addVocabulary(userId, request);
        return ResponseEntity.status(201).body(new ApiResponse<>(response));
    }

    @PutMapping("{vocabularyId}/v1")
    public ResponseEntity<ApiResponse<VocabularyResponse>> updateVocabulary(
            @RequestParam String userId,
            @PathVariable String vocabularyId,
            @RequestBody UpdateVocabularyRequest request
    ) {
        var response = vocabularyOrchestrationService.updateVocabulary(userId, vocabularyId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<VocabularyResponse>>> fetchVocabularies(
            @RequestParam String userId
    ) {
        var response = vocabularyOrchestrationService.fetchVocabularies(userId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("{vocabularyId}/v1")
    public ResponseEntity<ApiResponse<VocabularyResponse>> fetchVocabulary(
            @RequestParam String userId,
            @PathVariable String vocabularyId
    ) {
        var response = vocabularyOrchestrationService.fetchVocabulary(userId, vocabularyId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
