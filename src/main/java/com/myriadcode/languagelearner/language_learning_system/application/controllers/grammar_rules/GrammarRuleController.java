package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.*;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleDraftDetailsResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleDraftResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarRuleOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable("grammarRuleId") String grammarRuleId,
            @RequestBody EditGrammarRuleRequest request
    ) {
        var response = grammarRuleOrchestrationService.editGrammarRule(grammarRuleId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @DeleteMapping("{grammarRuleId}/explanation/v1")
    public ResponseEntity<ApiResponse<GrammarRuleResponse>> deleteGrammarRuleExplanation(
            @PathVariable("grammarRuleId") String grammarRuleId,
            @RequestBody DeleteGrammarRuleExplanationRequest request
    ) {
        var response = grammarRuleOrchestrationService.deleteGrammarRuleExplanation(grammarRuleId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("v1")
    public ResponseEntity<ApiResponse<List<GrammarRuleResponse>>> fetchGrammarRules() {
        var response = grammarRuleOrchestrationService.fetchGrammarRules();
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @GetMapping("{grammarRuleId}/v1")
    public ResponseEntity<ApiResponse<GrammarRuleResponse>> fetchGrammarRule(
            @PathVariable("grammarRuleId") String grammarRuleId
    ) {
        var response = grammarRuleOrchestrationService.fetchGrammarRule(grammarRuleId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @PostMapping("admin/drafts/v1")
    public ResponseEntity<ApiResponse<List<GrammarRuleDraftResponse>>> draftGrammarRules(
            @RequestBody DraftGrammarRulesRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(grammarRuleOrchestrationService.draftGrammarRules(request)));
    }

    @GetMapping("admin/drafts/v1")
    public ResponseEntity<ApiResponse<List<GrammarRuleDraftResponse>>> fetchDraftGrammarRules(
            @RequestParam("admin_key") String adminKey
    ) {
        return ResponseEntity.ok(new ApiResponse<>(grammarRuleOrchestrationService.fetchDraftGrammarRules(adminKey)));
    }

    @PostMapping("admin/drafts/{draftId}/details/v1")
    public ResponseEntity<ApiResponse<GrammarRuleDraftDetailsResponse>> generateDraftDetailsForDraftId(
            @PathVariable("draftId") String draftId,
            @RequestBody GenerateGrammarRuleDraftDetailsRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(grammarRuleOrchestrationService.generateDraftDetailsForDraftId(draftId, request)));
    }

    @PostMapping("admin/details/v1")
    public ResponseEntity<ApiResponse<List<GrammarRuleDraftDetailsResponse>>> generateGrammarRuleDetails(
            @RequestBody GenerateGrammarRuleDetailsRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(grammarRuleOrchestrationService.generateDraftDetails(request)));
    }

    @PostMapping("admin/approve/v1")
    public ResponseEntity<ApiResponse<List<GrammarRuleResponse>>> approveGrammarRules(
            @RequestBody ApproveGrammarRulesRequest request
    ) {
        return ResponseEntity.status(201).body(new ApiResponse<>(grammarRuleOrchestrationService.approveGrammarRules(request)));
    }
}
