package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentInput;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentProposal;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCurationLlmApi;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarLevelReassignmentSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenarioSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GrammarLevelReassignmentService {

    private final GrammarRuleRepo grammarRuleRepo;
    private final GrammarRuleCurationLlmApi grammarRuleCurationLlmApi;
    private final GrammarContentAuthorizationService authorizationService;

    public GrammarLevelReassignmentService(GrammarRuleRepo grammarRuleRepo,
                                           GrammarRuleCurationLlmApi grammarRuleCurationLlmApi,
                                           GrammarContentAuthorizationService authorizationService) {
        this.grammarRuleRepo = grammarRuleRepo;
        this.grammarRuleCurationLlmApi = grammarRuleCurationLlmApi;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public GrammarLevelReassignmentSummaryResponse reassignLevels(String userId) {
        authorizationService.requireAuthenticatedUser(userId);

        var grammarRules = grammarRuleRepo.findAll().stream()
                .filter(rule -> !"DRAFT".equalsIgnoreCase(rule.status()))
                .toList();
        var ruleById = grammarRules.stream()
                .collect(Collectors.toMap(rule -> rule.id().id(), Function.identity()));

        var proposals = grammarRuleCurationLlmApi.reassignGrammarLevels(grammarRules.stream()
                .map(this::toLlmInput)
                .toList());
        var validProposals = validate(proposals, ruleById);

        var changedRules = new ArrayList<GrammarLevelReassignmentSummaryResponse.ChangedRuleResponse>();
        for (GrammarLevelReassignmentProposal proposal : validProposals) {
            var existing = ruleById.get(proposal.grammarRuleId());
            var currentLevel = LanguageLevel.from(existing.level());
            var proposedLevel = proposal.proposedLevel();
            if (!proposal.changeRequired() || currentLevel == proposedLevel) {
                continue;
            }

            grammarRuleRepo.save(new GrammarRule(
                    existing.id(),
                    existing.identifier(),
                    existing.name(),
                    proposedLevel.name(),
                    existing.status(),
                    existing.active(),
                    existing.explanationParagraphs(),
                    existing.grammarScenario()
            ));
            changedRules.add(new GrammarLevelReassignmentSummaryResponse.ChangedRuleResponse(
                    existing.id().id(),
                    existing.name(),
                    currentLevel.name(),
                    proposedLevel.name(),
                    proposal.reason()
            ));
        }

        return new GrammarLevelReassignmentSummaryResponse(
                grammarRules.size(),
                changedRules.size(),
                grammarRules.size() - changedRules.size(),
                List.copyOf(changedRules)
        );
    }

    private GrammarLevelReassignmentInput toLlmInput(GrammarRule rule) {
        var examples = rule.grammarScenario() == null || rule.grammarScenario().sentences() == null
                ? List.<GrammarLevelReassignmentInput.GrammarExample>of()
                : rule.grammarScenario().sentences().stream()
                .map(this::toExample)
                .toList();
        return new GrammarLevelReassignmentInput(
                rule.id().id(),
                rule.name(),
                LanguageLevel.from(rule.level()).name(),
                rule.explanationParagraphs() == null ? List.of() : rule.explanationParagraphs().stream()
                        .map(paragraph -> paragraph.text())
                        .toList(),
                examples
        );
    }

    private GrammarLevelReassignmentInput.GrammarExample toExample(GrammarScenarioSentence sentence) {
        return new GrammarLevelReassignmentInput.GrammarExample(sentence.sentence(), sentence.translation());
    }

    private List<GrammarLevelReassignmentProposal> validate(List<GrammarLevelReassignmentProposal> proposals,
                                                            Map<String, GrammarRule> ruleById) {
        if (proposals == null) {
            throw new IllegalArgumentException("Grammar level reassignment response is required");
        }

        var seen = new HashSet<String>();
        for (GrammarLevelReassignmentProposal proposal : proposals) {
            if (proposal == null || proposal.grammarRuleId() == null || proposal.grammarRuleId().isBlank()) {
                throw new IllegalArgumentException("Grammar level reassignment result must include grammarRuleId");
            }
            if (!ruleById.containsKey(proposal.grammarRuleId())) {
                throw new IllegalArgumentException("Unknown grammar rule ID returned: " + proposal.grammarRuleId());
            }
            if (!seen.add(proposal.grammarRuleId())) {
                throw new IllegalArgumentException("Duplicate grammar rule ID returned: " + proposal.grammarRuleId());
            }
            if (proposal.currentLevel() == null) {
                throw new IllegalArgumentException("Current language level is required");
            }
            if (proposal.proposedLevel() == null) {
                throw new IllegalArgumentException("Proposed language level is required");
            }
        }
        return proposals;
    }
}
