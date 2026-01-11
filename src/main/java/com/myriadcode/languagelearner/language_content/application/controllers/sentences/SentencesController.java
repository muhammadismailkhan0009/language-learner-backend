package com.myriadcode.languagelearner.language_content.application.controllers.sentences;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_content.application.controllers.sentences.response.SentenceDataResponse;
import com.myriadcode.languagelearner.language_content.application.services.ContentQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/sentences")
public class SentencesController {

    @Autowired
    private ContentQueryService contentQueryService;

    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<SentenceDataResponse>>> fetchAllSentences() {
        var sentences = contentQueryService.fetchAllSentences();
        return ResponseEntity.ok(new ApiResponse<>(sentences));
    }
}
