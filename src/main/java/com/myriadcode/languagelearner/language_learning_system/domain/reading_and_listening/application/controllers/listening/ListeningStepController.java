package com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.application.controllers.listening;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.application.controllers.listening.request.WordToListenToRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.application.controllers.listening.response.WordToListenToResponse;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.application.services.ListeningStepOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.domain.listening.WordToListenTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/listening")
public class ListeningStepController {

    @Autowired
    private ListeningStepOrchestrationService listeningStepOrchestrationService;

    @PostMapping("v1")
    public ResponseEntity<Void> saveWordToListenTo(
            @RequestParam String userId,
            @RequestBody WordToListenToRequest request
    ) {
        listeningStepOrchestrationService.saveWordToListenTo(userId, new WordToListenTo(null, request.word()));
        return ResponseEntity.status(201).build();
    }

    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<WordToListenToResponse>>> fetchWordsToListenTo(
            @RequestParam String userId
    ) {
        var words = listeningStepOrchestrationService.fetchWordsToListenTo(userId);
        var response = words.stream().map(word -> new WordToListenToResponse(word.word())).toList();
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
