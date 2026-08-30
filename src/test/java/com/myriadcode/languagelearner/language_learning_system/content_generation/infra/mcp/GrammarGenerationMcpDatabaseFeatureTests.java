package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftProposal;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentProposal;
import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarGenerationRequestService;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarGenerationService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarGenerationRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_RULE_DRAFT;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_RULE_DETAILS;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_LEVEL_REASSIGNMENT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class GrammarGenerationMcpDatabaseFeatureTests {
    @Autowired private GrammarGenerationRequestService requestService;
    @Autowired private GrammarGenerationMcpTools mcpTools;
    @Autowired private ContentGenerationJobService jobService;
    @Autowired private GrammarGenerationRequestRepo requestRepo;
    @Autowired private GrammarRuleRepo grammarRuleRepo;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void createUsers() {
        jdbc.update("insert into user_info (id, username, password) values (?, ?, ?) on conflict do nothing",
                "user-1", "user-1", "test");
        jdbc.update("insert into user_info (id, username, password) values (?, ?, ?) on conflict do nothing",
                "other-user", "other-user", "test");
    }

    @Test
    void draftRequestIsFetchedAndStoredThroughMcpUsingPostgres() {
        requestService.requestRuleDrafts("user-1", "b1");

        assertThat(jobService.exists("user-1", GRAMMAR_RULE_DRAFT)).isTrue();
        assertThat(requestRepo.findOldestByUserIdAndType("user-1", GrammarGenerationRequest.Type.RULE_DRAFT))
                .isPresent();

        try (var ignored = McpUserContextHolder.scoped("other-user")) {
            assertThat(mcpTools.getRuleDrafting()).isNull();
        }

        GrammarGenerationService.GrammarPrompt prompt;
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            prompt = mcpTools.getRuleDrafting();
        }
        assertThat(prompt).isNotNull();
        assertThat(prompt.prompt()).contains("B1");

        var submission = new GrammarGenerationService.GrammarRuleDraftSubmission(prompt.requestId(), List.of(
                new GrammarRuleDraftProposal("weil-clause", "Weil clauses", "B1", "de")
        ));
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var response = mcpTools.storeRuleDrafting(submission);
            assertThat(response.success()).isTrue();
            assertThat(response.stored()).isEqualTo(1);
        }

        assertThat(grammarRuleRepo.findByStatus("DRAFT"))
                .singleElement()
                .satisfies(rule -> {
                    assertThat(rule.identifier()).isEqualTo("weil-clause");
                    assertThat(rule.level()).isEqualTo("B1");
                    assertThat(rule.grammarScenario().targetLanguage()).isEqualTo("de");
                });
        assertThat(jobService.exists("user-1", GRAMMAR_RULE_DRAFT)).isFalse();
        assertThat(requestRepo.findOldestByUserIdAndType("user-1", GrammarGenerationRequest.Type.RULE_DRAFT))
                .isEmpty();
    }

    @Test
    void detailRequestIsFetchedAndStoredThroughMcpUsingPostgres() {
        var draft = grammarRuleRepo.save(rule("detail-draft", "DRAFT", "A2"));
        requestService.requestRuleDetailsForDraft("user-1", draft.id().id());

        GrammarGenerationService.GrammarPrompt prompt;
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            prompt = mcpTools.getRuleDetails();
        }
        assertThat(prompt).isNotNull();
        assertThat(prompt.prompt()).contains("detail draft");

        var details = new GrammarRuleDraftDetails("detail-draft", "Detail draft", "A2", "de",
                List.of("This is the explanation."),
                List.of(new GrammarRuleDraftDetails.GrammarRuleExample(
                        "Das ist ein Beispiel.", "This is an example.", "")));
        var submission = new GrammarGenerationService.GrammarRuleDetailsSubmission(prompt.requestId(),
                List.of(new GrammarGenerationService.GeneratedRuleDetails(draft.id().id(), details)));
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(mcpTools.storeRuleDetails(submission).success()).isTrue();
        }

        assertThat(grammarRuleRepo.findById(draft.id().id())).get().satisfies(stored -> {
            assertThat(stored.status()).isEqualTo("READY");
            assertThat(stored.active()).isTrue();
            assertThat(stored.explanationParagraphs()).extracting("text")
                    .containsExactly("This is the explanation.");
            assertThat(stored.grammarScenario().sentences()).hasSize(1);
        });
        assertThat(jobService.exists("user-1", GRAMMAR_RULE_DETAILS)).isFalse();
        assertThat(requestRepo.findOldestByUserIdAndType("user-1", GrammarGenerationRequest.Type.RULE_DETAILS))
                .isEmpty();
    }

    @Test
    void levelReassignmentIsFetchedAndStoredThroughMcpUsingPostgres() {
        var readyRule = grammarRuleRepo.save(rule("reassign-rule", "READY", "A1"));
        requestService.requestLevelReassignment("user-1");

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(mcpTools.getLevelReassignment()).contains("reassign rule");
            var response = mcpTools.storeLevelReassignment(List.of(new GrammarLevelReassignmentProposal(
                    readyRule.id().id(), LanguageLevel.A1, LanguageLevel.B1, true, "Requires B1")));
            assertThat(response.success()).isTrue();
            assertThat(response.stored()).isEqualTo(1);
        }

        assertThat(grammarRuleRepo.findById(readyRule.id().id())).get()
                .extracting(GrammarRule::level).isEqualTo("B1");
        assertThat(jobService.exists("user-1", GRAMMAR_LEVEL_REASSIGNMENT)).isFalse();
    }

    private GrammarRule rule(String id, String status, String level) {
        return new GrammarRule(new GrammarRule.GrammarRuleId(id), id, id.replace('-', ' '), level, status,
                "READY".equals(status), List.of(), new GrammarScenario(
                new GrammarScenario.GrammarScenarioId(id + "-scenario"), "Examples", "Examples", "de",
                "SYSTEM", false, List.of()));
    }
}
