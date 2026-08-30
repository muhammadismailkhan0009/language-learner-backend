package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.*;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.*;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarGenerationRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.*;

@Service
public class GrammarGenerationService {
    private final GrammarGenerationRequestRepo requestRepo;
    private final GrammarRuleRepo grammarRuleRepo;
    private final ContentGenerationJobService jobService;
    private final GrammarGenerationPromptApi promptApi;

    public GrammarGenerationService(GrammarGenerationRequestRepo requestRepo, GrammarRuleRepo grammarRuleRepo,
                                    ContentGenerationJobService jobService, GrammarGenerationPromptApi promptApi) {
        this.requestRepo = requestRepo;
        this.grammarRuleRepo = grammarRuleRepo;
        this.jobService = jobService;
        this.promptApi = promptApi;
    }

    @Transactional(readOnly = true)
    public GrammarPrompt prepareRuleDraftPrompt(String userId) {
        jobService.require(userId, GRAMMAR_RULE_DRAFT);
        var request = requireRequest(userId, GrammarGenerationRequest.Type.RULE_DRAFT);
        var catalog = grammarRuleRepo.findAll().stream()
                .map(rule -> new GrammarRuleCatalogContext(rule.identifier(), rule.name(), rule.level())).toList();
        return new GrammarPrompt(request.id().id(),
                promptApi.ruleDraftPrompt(request.level(), request.targetLanguage(), 12, catalog));
    }

    @Transactional
    public int storeRuleDrafts(String userId, GrammarRuleDraftSubmission submission) {
        jobService.require(userId, GRAMMAR_RULE_DRAFT);
        var request = matchingRequest(userId, GrammarGenerationRequest.Type.RULE_DRAFT, submission.requestId());
        var proposals = submission.rules() == null ? List.<GrammarRuleDraftProposal>of() : submission.rules();
        proposals.stream().map(this::newDraft).forEach(grammarRuleRepo::save);
        completeRequest(userId, request, GrammarGenerationRequest.Type.RULE_DRAFT, GRAMMAR_RULE_DRAFT);
        return proposals.size();
    }

    @Transactional(readOnly = true)
    public GrammarPrompt prepareRuleDetailsPrompt(String userId) {
        jobService.require(userId, GRAMMAR_RULE_DETAILS);
        var request = requireRequest(userId, GrammarGenerationRequest.Type.RULE_DETAILS);
        return new GrammarPrompt(request.id().id(),
                promptApi.ruleDetailsPrompt(request.level(), request.targetLanguage(), request.rules()));
    }

    @Transactional
    public int storeRuleDetails(String userId, GrammarRuleDetailsSubmission submission) {
        jobService.require(userId, GRAMMAR_RULE_DETAILS);
        var request = matchingRequest(userId, GrammarGenerationRequest.Type.RULE_DETAILS, submission.requestId());
        var allowedDraftIds = request.rules().stream().map(GrammarGenerationRequest.RuleSeed::draftId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        var results = submission.rules() == null ? List.<GeneratedRuleDetails>of() : submission.rules();
        for (var result : results) {
            if (!allowedDraftIds.contains(result.draftId())) throw new IllegalArgumentException("Unknown grammar draft ID");
            var existing = grammarRuleRepo.findById(result.draftId())
                    .orElseThrow(() -> new IllegalArgumentException("Grammar draft not found"));
            grammarRuleRepo.save(withDetails(existing, result.details()));
        }
        completeRequest(userId, request, GrammarGenerationRequest.Type.RULE_DETAILS, GRAMMAR_RULE_DETAILS);
        return results.size();
    }

    @Transactional(readOnly = true)
    public String prepareLevelReassignmentPrompt(String userId) {
        jobService.require(userId, GRAMMAR_LEVEL_REASSIGNMENT);
        return promptApi.levelReassignmentPrompt(grammarRuleRepo.findAll().stream()
                .filter(rule -> !"DRAFT".equalsIgnoreCase(rule.status())).map(this::toReassignmentInput).toList());
    }

    @Transactional
    public int storeLevelReassignments(String userId, List<GrammarLevelReassignmentProposal> proposals) {
        jobService.require(userId, GRAMMAR_LEVEL_REASSIGNMENT);
        var rules = grammarRuleRepo.findAll().stream().filter(rule -> !"DRAFT".equalsIgnoreCase(rule.status()))
                .collect(Collectors.toMap(rule -> rule.id().id(), Function.identity()));
        var seen = new HashSet<String>();
        int changed = 0;
        for (var proposal : proposals == null ? List.<GrammarLevelReassignmentProposal>of() : proposals) {
            var existing = rules.get(proposal.grammarRuleId());
            if (existing == null || !seen.add(proposal.grammarRuleId()))
                throw new IllegalArgumentException("Invalid grammar level reassignment result");
            if (proposal.changeRequired() && LanguageLevel.from(existing.level()) != proposal.proposedLevel()) {
                grammarRuleRepo.save(new GrammarRule(existing.id(), existing.identifier(), existing.name(),
                        proposal.proposedLevel().name(), existing.status(), existing.active(),
                        existing.explanationParagraphs(), existing.grammarScenario()));
                changed++;
            }
        }
        jobService.delete(userId, GRAMMAR_LEVEL_REASSIGNMENT);
        return changed;
    }

    private GrammarGenerationRequest requireRequest(String userId, GrammarGenerationRequest.Type type) {
        return requestRepo.findOldestByUserIdAndType(userId, type)
                .orElseThrow(() -> new IllegalStateException("No grammar generation request found"));
    }

    private GrammarGenerationRequest matchingRequest(String userId, GrammarGenerationRequest.Type type, String id) {
        var request = requireRequest(userId, type);
        if (id == null || !request.id().id().equals(id)) throw new IllegalArgumentException("Unknown grammar request ID");
        return request;
    }

    private void completeRequest(String userId, GrammarGenerationRequest request,
                                 GrammarGenerationRequest.Type requestType,
                                 com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType jobType) {
        requestRepo.delete(request.id());
        if (!requestRepo.existsByUserIdAndType(userId, requestType)) jobService.delete(userId, jobType);
    }

    private GrammarRule newDraft(GrammarRuleDraftProposal value) {
        return new GrammarRule(new GrammarRule.GrammarRuleId(UUID.randomUUID().toString()), value.identifier(), value.name(),
                value.level(), "DRAFT", false, List.of(), new GrammarScenario(
                new GrammarScenario.GrammarScenarioId(UUID.randomUUID().toString()), "Explanation examples",
                "Examples for grammar rule", value.targetLanguage(), "LLM", false, List.of()));
    }

    private GrammarRule withDetails(GrammarRule existing, GrammarRuleDraftDetails details) {
        var paragraphs = details.explanationParagraphs().stream().map(text -> new GrammarExplanationParagraph(
                new GrammarExplanationParagraph.GrammarExplanationParagraphId(UUID.randomUUID().toString()), text, 0)).toList();
        var sentences = details.explanationExamples().stream().map(example -> new GrammarScenarioSentence(
                new GrammarScenarioSentence.GrammarScenarioSentenceId(UUID.randomUUID().toString()),
                example.sentence(), example.translation(), 0)).toList();
        var oldScenario = existing.grammarScenario();
        var scenario = new GrammarScenario(oldScenario.id(), oldScenario.title(), oldScenario.description(),
                oldScenario.targetLanguage(), oldScenario.createdBy(), oldScenario.isFixed(), sentences);
        return new GrammarRule(existing.id(), details.identifier(), details.name(), details.level(), "READY", true,
                paragraphs, scenario);
    }

    private GrammarLevelReassignmentInput toReassignmentInput(GrammarRule rule) {
        var examples = rule.grammarScenario() == null ? List.<GrammarLevelReassignmentInput.GrammarExample>of()
                : rule.grammarScenario().sentences().stream().map(sentence ->
                new GrammarLevelReassignmentInput.GrammarExample(sentence.sentence(), sentence.translation())).toList();
        return new GrammarLevelReassignmentInput(rule.id().id(), rule.name(), rule.level(),
                rule.explanationParagraphs().stream().map(GrammarExplanationParagraph::text).toList(), examples);
    }

    public record GrammarPrompt(String requestId, String prompt) {}
    public record GrammarRuleDraftSubmission(String requestId, List<GrammarRuleDraftProposal> rules) {}
    public record GrammarRuleDetailsSubmission(String requestId, List<GeneratedRuleDetails> rules) {}
    public record GeneratedRuleDetails(String draftId, GrammarRuleDraftDetails details) {}
}
