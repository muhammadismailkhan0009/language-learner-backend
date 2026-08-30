package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarGenerationRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_LEVEL_REASSIGNMENT;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_RULE_DETAILS;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.GRAMMAR_RULE_DRAFT;

@Service
public class GrammarGenerationRequestService {
    private final GrammarGenerationRequestRepo requestRepo;
    private final ContentGenerationJobService jobService;
    private final GrammarRuleRepo grammarRuleRepo;

    public GrammarGenerationRequestService(GrammarGenerationRequestRepo requestRepo,
                                           ContentGenerationJobService jobService,
                                           GrammarRuleRepo grammarRuleRepo) {
        this.requestRepo = requestRepo;
        this.jobService = jobService;
        this.grammarRuleRepo = grammarRuleRepo;
    }

    @Transactional
    public String requestRuleDrafts(String userId, String level) {
        String normalizedUserId = requireUserId(userId);
        requestRepo.save(new GrammarGenerationRequest(
                new GrammarGenerationRequest.GrammarGenerationRequestId(UUID.randomUUID().toString()),
                new UserId(normalizedUserId), GrammarGenerationRequest.Type.RULE_DRAFT,
                normalizeLevel(level), "de", List.of(), Instant.now()));
        jobService.createOrReplace(normalizedUserId, GRAMMAR_RULE_DRAFT);
        return "Grammar rule drafting requested. Run your MCP tool.";
    }

    @Transactional
    public String requestRuleDetails(String userId, String level, String targetLanguage,
                                     List<GrammarGenerationRequest.RuleSeed> rules) {
        String normalizedUserId = requireUserId(userId);
        if (rules == null || rules.isEmpty()) throw new IllegalArgumentException("rules are required");
        requestRepo.save(new GrammarGenerationRequest(
                new GrammarGenerationRequest.GrammarGenerationRequestId(UUID.randomUUID().toString()),
                new UserId(normalizedUserId), GrammarGenerationRequest.Type.RULE_DETAILS,
                normalizeLevel(level), normalizeLanguage(targetLanguage), rules, Instant.now()));
        jobService.createOrReplace(normalizedUserId, GRAMMAR_RULE_DETAILS);
        return "Grammar rule details requested. Run your MCP tool.";
    }

    public String requestRuleDetailsForDraft(String userId, String draftId) {
        var draft = grammarRuleRepo.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Grammar draft not found"));
        if (!"DRAFT".equalsIgnoreCase(draft.status())) {
            throw new IllegalArgumentException("Grammar rule is not in draft status");
        }
        return requestRuleDetails(userId, draft.level(),
                draft.grammarScenario() == null ? "de" : draft.grammarScenario().targetLanguage(),
                List.of(new GrammarGenerationRequest.RuleSeed(
                        draft.id().id(), draft.identifier(), draft.name())));
    }

    @Transactional
    public String requestLevelReassignment(String userId) {
        String normalizedUserId = requireUserId(userId);
        jobService.createOrReplace(normalizedUserId, GRAMMAR_LEVEL_REASSIGNMENT);
        return "Grammar level reassignment requested. Run your MCP tool.";
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
        return userId.trim();
    }

    private String normalizeLevel(String level) {
        return level == null || level.isBlank() ? "A1" : level.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeLanguage(String targetLanguage) {
        return targetLanguage == null || targetLanguage.isBlank() ? "de" : targetLanguage.trim();
    }
}
