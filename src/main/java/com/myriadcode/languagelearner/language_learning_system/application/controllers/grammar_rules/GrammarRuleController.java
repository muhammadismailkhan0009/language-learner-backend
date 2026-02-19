package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.CreateGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.EditGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarRuleOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/grammar-rules")
public class GrammarRuleController {

    private final GrammarRuleOrchestrationService grammarRuleOrchestrationService;

    public GrammarRuleController(GrammarRuleOrchestrationService grammarRuleOrchestrationService) {
        this.grammarRuleOrchestrationService = grammarRuleOrchestrationService;
    }

    @PostMapping("v1")
    public ResponseEntity<ApiResponse<GrammarRuleResponse>> createGrammarRule(
            @RequestBody CreateGrammarRuleRequest request
    ) {
        var response = grammarRuleOrchestrationService.createGrammarRule(request);
        return ResponseEntity.status(201).body(new ApiResponse<>(response));
    }

    @PutMapping("{grammarRuleId}/v1")
    public ResponseEntity<ApiResponse<GrammarRuleResponse>> editGrammarRule(
            @PathVariable String grammarRuleId,
            @RequestBody EditGrammarRuleRequest request
    ) {
        var response = grammarRuleOrchestrationService.editGrammarRule(grammarRuleId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<GrammarRuleResponse>>> fetchGrammarRules() {
        var response = grammarRuleOrchestrationService.fetchGrammarRules();
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("{grammarRuleId}/v1")
    public ResponseEntity<ApiResponse<GrammarRuleResponse>> fetchGrammarRule(@PathVariable String grammarRuleId) {
        var response = grammarRuleOrchestrationService.fetchGrammarRule(grammarRuleId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
