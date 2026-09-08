package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.SubmitVocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.SubmitVocabularyExtractionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyClozeGenerationService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/vocabularies")
public class VocabularyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyController.class);
    private final VocabularyOrchestrationService vocabularyOrchestrationService;
    private final VocabularyClozeGenerationService vocabularyClozeGenerationService;
    private final VocabularyExtractionService vocabularyExtractionService;

    public VocabularyController(VocabularyOrchestrationService vocabularyOrchestrationService,
                                VocabularyClozeGenerationService vocabularyClozeGenerationService,
                                VocabularyExtractionService vocabularyExtractionService) {
        this.vocabularyOrchestrationService = vocabularyOrchestrationService;
        this.vocabularyClozeGenerationService = vocabularyClozeGenerationService;
        this.vocabularyExtractionService = vocabularyExtractionService;
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
        LOGGER.warn("REST vocabulary fetch started: userId={}", userId);
        var response = vocabularyOrchestrationService.fetchVocabularies(userId);
        var countsByReverseFlashcardState = response.stream()
                .map(VocabularyResponse::reverseFlashcardState)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        TreeMap::new,
                        Collectors.counting()
                ));
        var attachedCount = countsByReverseFlashcardState.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        LOGGER.warn(
                "REST vocabulary response ready: userId={}, vocabularyCount={}, attachedCount={}, unattachedCount={}, countsByState={}",
                userId,
                response.size(),
                attachedCount,
                response.size() - attachedCount,
                countsByReverseFlashcardState
        );
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("songs-selection/v1")
    public ResponseEntity<ApiResponse<List<VocabularyResponse>>> fetchSongVocabularies(
            @RequestParam String userId,
            @RequestParam(required = false) Integer limit
    ) {
        var response = vocabularyOrchestrationService.fetchSongVocabularies(userId, limit);
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

    @PostMapping("cloze-sentences/v1")
    public ResponseEntity<ApiResponse<GenerateVocabularyClozeSentencesResponse>> generateClozeSentences(
            @RequestParam String userId
    ) {
        var response = vocabularyClozeGenerationService.generate(userId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @PostMapping("extractions/v1")
    public ResponseEntity<ApiResponse<SubmitVocabularyExtractionResponse>> submitExtraction(
            @Valid @RequestBody SubmitVocabularyExtractionRequest request
    ) {
        var extraction = vocabularyExtractionService.submit(request.userId(), request.sourceText());
        return ResponseEntity.accepted().body(new ApiResponse<>(new SubmitVocabularyExtractionResponse(
                extraction.id().id(),
                "Vocabulary extraction requested. Run your MCP tool."
        )));
    }

}
