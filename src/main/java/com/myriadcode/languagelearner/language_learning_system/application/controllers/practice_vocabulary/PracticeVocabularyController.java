package com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.request.ExtractPracticeVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.practice_vocabulary.response.ExtractPracticeVocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/practice-vocabulary")
public class PracticeVocabularyController {

    private final PracticeVocabularyService practiceVocabularyService;

    public PracticeVocabularyController(PracticeVocabularyService practiceVocabularyService) {
        this.practiceVocabularyService = practiceVocabularyService;
    }

    @PostMapping("extract")
    public ResponseEntity<ApiResponse<ExtractPracticeVocabularyResponse>> extract(
            @RequestBody ExtractPracticeVocabularyRequest request
    ) {
        var response = practiceVocabularyService.extractAndStore(request.userId(), request.text());
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
