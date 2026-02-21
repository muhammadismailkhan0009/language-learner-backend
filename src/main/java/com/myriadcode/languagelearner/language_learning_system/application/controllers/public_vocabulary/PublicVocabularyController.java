package com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.request.PublishPublicVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.response.PublicVocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.public_vocabulary.PublicVocabularyOrchestrationService;
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

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/public-vocabularies")
public class PublicVocabularyController {

    private final PublicVocabularyOrchestrationService publicVocabularyOrchestrationService;

    public PublicVocabularyController(PublicVocabularyOrchestrationService publicVocabularyOrchestrationService) {
        this.publicVocabularyOrchestrationService = publicVocabularyOrchestrationService;
    }

    @PostMapping("{vocabularyId}/v1")
    public ResponseEntity<ApiResponse<PublicVocabularyResponse>> publishVocabulary(
            @RequestParam String userId,
            @PathVariable String vocabularyId,
            @RequestBody PublishPublicVocabularyRequest request
    ) {
        var response = publicVocabularyOrchestrationService.publishVocabulary(userId, vocabularyId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<PublicVocabularyResponse>>> fetchPublicVocabularies() {
        var response = publicVocabularyOrchestrationService.fetchPublicVocabularies();
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
