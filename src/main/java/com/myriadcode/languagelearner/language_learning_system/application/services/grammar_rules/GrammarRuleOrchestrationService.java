package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.CreateGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.EditGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleResponse;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.grammar_rules.GrammarRuleApiMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services.GrammarRuleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrammarRuleOrchestrationService {

    private static final String ADMIN_KEY = "112233";
    private static final GrammarRuleApiMapper GRAMMAR_RULE_API_MAPPER = GrammarRuleApiMapper.INSTANCE;
    private final GrammarRuleRepo grammarRuleRepo;

    public GrammarRuleResponse createGrammarRule(CreateGrammarRuleRequest request) {
        validateAdminKey(request.adminKey());
        var toSave = GrammarRuleDomainService.create(
                request.name(),
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
                request.name(),
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

    private void validateAdminKey(String adminKey) {
        if (adminKey == null || !ADMIN_KEY.equals(adminKey)) {
            throw new IllegalArgumentException("Invalid admin key");
        }
    }
}
