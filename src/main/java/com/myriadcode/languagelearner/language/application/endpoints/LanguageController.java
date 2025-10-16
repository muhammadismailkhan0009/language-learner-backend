package com.myriadcode.languagelearner.language.application.endpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/language")
public class LanguageController {

    @PostMapping("flashcards")
    public ResponseEntity<Void> generateFlashCards(@RequestBody ApiRequest<LanguageScenarioRequest> flashcards) {
        System.out.println("Generating flashcards");

        return ResponseEntity.created(null).build();
    }


//    request records

    public record LanguageScenarioRequest(String scenario) {
    }

}
