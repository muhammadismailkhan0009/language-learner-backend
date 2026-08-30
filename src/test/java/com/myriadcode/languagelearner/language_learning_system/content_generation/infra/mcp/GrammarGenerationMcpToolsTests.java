package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarGenerationService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_RULE_DETAILS;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_RULE_DRAFT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(GrammarGenerationMcpTools.class)
class GrammarGenerationMcpToolsTests {
    @Autowired private GrammarGenerationMcpTools tools;
    @MockitoBean private ContentGenerationJobService jobService;
    @MockitoBean private GrammarGenerationService generationService;

    @Test
    void draftingToolMapsAuthenticatedUserToApplicationBoundary() {
        var expected = new GrammarGenerationService.GrammarPrompt("request-1", "prompt");
        when(jobService.exists("user-1", GRAMMAR_RULE_DRAFT)).thenReturn(true);
        when(generationService.prepareRuleDraftPrompt("user-1")).thenReturn(expected);

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getRuleDrafting()).isEqualTo(expected);
        }

        verify(jobService).exists("user-1", GRAMMAR_RULE_DRAFT);
        verify(generationService).prepareRuleDraftPrompt("user-1");
    }

    @Test
    void detailStoreMapsAuthenticatedUserAndReturnsAcknowledgement() {
        var submission = new GrammarGenerationService.GrammarRuleDetailsSubmission("request-1", List.of());
        when(generationService.storeRuleDetails("user-1", submission)).thenReturn(2);

        GrammarGenerationStoreResponse response;
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            response = tools.storeRuleDetails(submission);
        }

        assertThat(response.success()).isTrue();
        assertThat(response.stored()).isEqualTo(2);
        verify(generationService).storeRuleDetails("user-1", submission);
    }

    @Test
    void detailToolReturnsNoWorkWhenUserHasNoPendingJob() {
        when(jobService.exists("user-1", GRAMMAR_RULE_DETAILS)).thenReturn(false);

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getRuleDetails()).isNull();
        }

        verify(jobService).exists("user-1", GRAMMAR_RULE_DETAILS);
    }
}
