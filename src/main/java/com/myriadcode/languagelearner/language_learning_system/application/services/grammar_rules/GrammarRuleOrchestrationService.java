package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCurationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.*;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleDraftDetailsResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleDraftResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleResponse;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.grammar_rules.GrammarRuleApiMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services.GrammarRuleDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class GrammarRuleOrchestrationService {

    private static final String ADMIN_KEY = "112233";
    private static final GrammarRuleApiMapper GRAMMAR_RULE_API_MAPPER = GrammarRuleApiMapper.INSTANCE;
    private final GrammarRuleRepo grammarRuleRepo;
    private final GrammarRuleCurationLlmApi grammarRuleCurationLlmApi;

    @Autowired
    public GrammarRuleOrchestrationService(GrammarRuleRepo grammarRuleRepo,
                                           GrammarRuleCurationLlmApi grammarRuleCurationLlmApi) {
        this.grammarRuleRepo = grammarRuleRepo;
        this.grammarRuleCurationLlmApi = grammarRuleCurationLlmApi;
    }

    public GrammarRuleOrchestrationService(GrammarRuleRepo grammarRuleRepo) {
        this(grammarRuleRepo, new GrammarRuleCurationLlmApi() {
            @Override
            public List<com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftProposal> proposeRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
                return List.of();
            }

            @Override
            public com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails generateRuleDetails(String identifier, String name, String level, String targetLanguage) {
                return new com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails(
                        identifier, name, level, targetLanguage, List.of(), List.of()
                );
            }
        });
    }

    public GrammarRuleResponse createGrammarRule(CreateGrammarRuleRequest request) {
        validateAdminKey(request.adminKey());
        var toSave = GrammarRuleDomainService.create(
                normalizeIdentifier(request.identifier(), request.name()),
                request.name(),
                defaultLevel(request.level()),
                request.active() == null || request.active(),
                request.explanationParagraphs(),
                request.scenario() == null ? null : GRAMMAR_RULE_API_MAPPER.toCreateScenarioInput(request.scenario())
        );
        return GRAMMAR_RULE_API_MAPPER.toResponse(grammarRuleRepo.save(toSave));
    }

    public GrammarRuleResponse editGrammarRule(String grammarRuleId, EditGrammarRuleRequest request) {
        validateAdminKey(request.adminKey());
        var existing = grammarRuleRepo.findById(grammarRuleId)
                .orElseThrow(() -> new IllegalArgumentException("Grammar rule not found"));

        var toSave = GrammarRuleDomainService.edit(
                existing,
                normalizeIdentifier(request.identifier(), request.name() == null ? existing.name() : request.name()),
                request.name(),
                request.level(),
                request.active(),
                request.explanationParagraphs(),
                request.scenario() == null ? null : GRAMMAR_RULE_API_MAPPER.toPatchScenarioInput(request.scenario())
        );
        return GRAMMAR_RULE_API_MAPPER.toResponse(grammarRuleRepo.save(toSave));
    }

    public List<GrammarRuleResponse> fetchGrammarRules() {
        return grammarRuleRepo.findAll().stream()
                .map(GRAMMAR_RULE_API_MAPPER::toResponse)
                .toList();
    }

    public GrammarRuleResponse fetchGrammarRule(String grammarRuleId) {
        var grammarRule = grammarRuleRepo.findById(grammarRuleId)
                .orElseThrow(() -> new IllegalArgumentException("Grammar rule not found"));
        return GRAMMAR_RULE_API_MAPPER.toResponse(grammarRule);
    }

    public List<GrammarRuleDraftResponse> draftGrammarRules(DraftGrammarRulesRequest request) {
        validateAdminKey(request.adminKey());
        int count = 12;
        String level = defaultLevel(request.level());
        String targetLanguage = "de";
        List<GrammarRuleCatalogContext> existingRules = grammarRuleRepo.findAll().stream()
                .map(rule -> new GrammarRuleCatalogContext(
                        normalizeIdentifier(rule.identifier(), rule.name()),
                        rule.name(),
                        defaultLevel(rule.level())
                ))
                .toList();

        return grammarRuleCurationLlmApi.proposeRules(level, targetLanguage, count, existingRules).stream()
                .map(rule -> new GrammarRuleDraftResponse(
                        normalizeIdentifier(rule.identifier(), rule.name()),
                        rule.name(),
                        defaultLevel(rule.level()),
                        targetLanguage
                ))
                .toList();
    }

    public List<GrammarRuleDraftDetailsResponse> generateDraftDetails(GenerateGrammarRuleDetailsRequest request) {
        validateAdminKey(request.adminKey());
        if (request.rules() == null || request.rules().isEmpty()) {
            return List.of();
        }
        String level = defaultLevel(request.level());
        String targetLanguage = (request.targetLanguage() == null || request.targetLanguage().isBlank()) ? "de" : request.targetLanguage().trim();

        return request.rules().stream()
                .map(seed -> grammarRuleCurationLlmApi.generateRuleDetails(
                        normalizeIdentifier(seed.identifier(), seed.name()),
                        seed.name(),
                        level,
                        targetLanguage
                ))
                .map(details -> new GrammarRuleDraftDetailsResponse(
                        normalizeIdentifier(details.identifier(), details.name()),
                        details.name(),
                        defaultLevel(details.level()),
                        targetLanguage,
                        details.explanationParagraphs(),
                        details.explanationExamples().stream()
                                .map(example -> new GrammarRuleResponse.GrammarExplanationExampleResponse(
                                        example.sentence(),
                                        example.translation(),
                                        example.note()
                                ))
                                .toList()
                ))
                .toList();
    }

    public List<GrammarRuleResponse> approveGrammarRules(ApproveGrammarRulesRequest request) {
        validateAdminKey(request.adminKey());
        if (request.rules() == null || request.rules().isEmpty()) {
            return List.of();
        }

        return request.rules().stream().map(rule -> {
            var scenarioInput = new GrammarRuleDomainService.GrammarScenarioCreateInput(
                    "Explanation examples",
                    "Examples for grammar rule",
                    (rule.targetLanguage() == null || rule.targetLanguage().isBlank()) ? "de" : rule.targetLanguage(),
                    (rule.explanationExamples() == null ? List.<ApproveGrammarRulesRequest.Example>of() : rule.explanationExamples()).stream()
                            .map(example -> new GrammarRuleDomainService.GrammarScenarioSentenceInput(
                                    example.sentence(),
                                    example.translation()
                            ))
                            .toList()
            );
            var toSave = GrammarRuleDomainService.create(
                    normalizeIdentifier(rule.identifier(), rule.name()),
                    rule.name(),
                    defaultLevel(rule.level()),
                    rule.active() == null || rule.active(),
                    rule.explanationParagraphs(),
                    scenarioInput
            );
            return GRAMMAR_RULE_API_MAPPER.toResponse(grammarRuleRepo.save(toSave));
        }).toList();
    }

    private String defaultLevel(String level) {
        if (level == null || level.isBlank()) {
            return "A1";
        }
        return level.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeIdentifier(String identifier, String fallbackName) {
        String raw = (identifier == null || identifier.isBlank()) ? fallbackName : identifier;
        if (raw == null || raw.isBlank()) {
            return "grammar-rule";
        }
        return raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private void validateAdminKey(String adminKey) {
        if (adminKey == null || !ADMIN_KEY.equals(adminKey)) {
            throw new IllegalArgumentException("Invalid admin key");
        }
    }
}
