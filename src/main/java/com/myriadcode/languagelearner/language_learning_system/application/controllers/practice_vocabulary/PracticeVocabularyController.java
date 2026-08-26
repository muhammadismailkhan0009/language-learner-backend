package com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.request.ExtractPracticeVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.response.ExtractPracticeVocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyExtractionRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/practice-vocabulary")
public class PracticeVocabularyController {

    private final PracticeVocabularyExtractionRequestService extractionRequestService;

    public PracticeVocabularyController(PracticeVocabularyExtractionRequestService extractionRequestService) {
        this.extractionRequestService = extractionRequestService;
    }

    @PostMapping("extract")
    public ResponseEntity<ApiResponse<ExtractPracticeVocabularyResponse>> extract(
            @RequestBody ExtractPracticeVocabularyRequest request
    ) {
        var message = extractionRequestService.request(request.userId(), request.text());
        return ResponseEntity.accepted().body(new ApiResponse<>(
                new ExtractPracticeVocabularyResponse(message)
        ));
    }
}
