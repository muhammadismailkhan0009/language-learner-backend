package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentProposal;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarGenerationService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.*;

@Service
public class GrammarGenerationMcpTools {
    private final ContentGenerationJobService jobService;
    private final GrammarGenerationService generationService;

    public GrammarGenerationMcpTools(ContentGenerationJobService jobService,
                                     GrammarGenerationService generationService) {
        this.jobService = jobService;
        this.generationService = generationService;
    }

    @McpTool(name = "get_grammar_rule_drafting", description = "Fetch pending grammar-rule drafting prompt.")
    public GrammarGenerationService.GrammarPrompt getRuleDrafting() {
        var userId = McpUserContextHolder.requireUserId();
        return jobService.exists(userId, GRAMMAR_RULE_DRAFT) ? generationService.prepareRuleDraftPrompt(userId) : null;
    }

    @McpTool(name = "store_grammar_rule_drafting", description = "Store generated grammar-rule drafts.")
    public GrammarGenerationStoreResponse storeRuleDrafting(
            @McpToolParam(description = "Request ID and generated grammar-rule drafts", required = true)
            GrammarGenerationService.GrammarRuleDraftSubmission submission) {
        return store(() -> generationService.storeRuleDrafts(McpUserContextHolder.requireUserId(), submission));
    }

    @McpTool(name = "get_grammar_rule_details", description = "Fetch pending grammar-rule details prompt.")
    public GrammarGenerationService.GrammarPrompt getRuleDetails() {
        var userId = McpUserContextHolder.requireUserId();
        return jobService.exists(userId, GRAMMAR_RULE_DETAILS) ? generationService.prepareRuleDetailsPrompt(userId) : null;
    }

    @McpTool(name = "store_grammar_rule_details", description = "Validate and store generated grammar-rule details.")
    public GrammarGenerationStoreResponse storeRuleDetails(
            @McpToolParam(description = "Request ID and generated details keyed by draft ID", required = true)
            GrammarGenerationService.GrammarRuleDetailsSubmission submission) {
        return store(() -> generationService.storeRuleDetails(McpUserContextHolder.requireUserId(), submission));
    }

    @McpTool(name = "get_grammar_level_reassignment", description = "Fetch pending grammar-level reassignment prompt.")
    public String getLevelReassignment() {
        var userId = McpUserContextHolder.requireUserId();
        return jobService.exists(userId, GRAMMAR_LEVEL_REASSIGNMENT)
                ? generationService.prepareLevelReassignmentPrompt(userId) : "";
    }

    @McpTool(name = "store_grammar_level_reassignment", description = "Validate and store grammar-level changes.")
    public GrammarGenerationStoreResponse storeLevelReassignment(
            @McpToolParam(description = "Grammar-level reassignment proposals", required = true)
            List<GrammarLevelReassignmentProposal> proposals) {
        return store(() -> generationService.storeLevelReassignments(McpUserContextHolder.requireUserId(), proposals));
    }

    private GrammarGenerationStoreResponse store(StoreOperation operation) {
        try {
            return new GrammarGenerationStoreResponse(true, List.of(), operation.execute());
        } catch (IllegalArgumentException exception) {
            return new GrammarGenerationStoreResponse(false, List.of(exception.getMessage()), null);
        }
    }

    @FunctionalInterface
    private interface StoreOperation { int execute(); }
}
