package com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice.request.CreateWordPracticeGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice.request.SubmitWordPracticeAnswerRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice.response.WordPracticeQueueResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeAnswerService;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationRequestService;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeQueryService;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services.WordPracticeCapacityPolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/word-practice")
public class WordPracticeController {
    private final WordPracticeGenerationRequestService generationRequestService;
    private final WordPracticeQueryService queryService;
    private final WordPracticeAnswerService answerService;

    public WordPracticeController(WordPracticeGenerationRequestService generationRequestService,
                                  WordPracticeQueryService queryService,
                                  WordPracticeAnswerService answerService) {
        this.generationRequestService = generationRequestService;
        this.queryService = queryService;
        this.answerService = answerService;
    }

    @GetMapping
    public ApiResponse<WordPracticeQueueResponse> findAll(@RequestParam String userId) {
        var practices = queryService.findAll(userId);
        return new ApiResponse<>(new WordPracticeQueueResponse(
                practices.stream().map(practice -> practice.vocabularyId()).distinct().toList().size(),
                WordPracticeCapacityPolicy.MAXIMUM_ACTIVE_VOCABULARY,
                WordPracticeCapacityPolicy.GENERATION_VOCABULARY_COUNT,
                practices
        ));
    }

    @PostMapping("generation")
    public ResponseEntity<ApiResponse<String>> requestGeneration(
            @RequestBody CreateWordPracticeGenerationRequest request
    ) {
        return ResponseEntity.accepted()
                .body(new ApiResponse<>(generationRequestService.request(request.userId())));
    }

    @PostMapping("{practiceId}/answer")
    public ApiResponse<Boolean> submitAnswer(@PathVariable String practiceId,
                                             @RequestBody SubmitWordPracticeAnswerRequest request) {
        return new ApiResponse<>(answerService.submit(request.userId(), practiceId, request.answer()));
    }
}
