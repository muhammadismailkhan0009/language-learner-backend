package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record GrammarGenerationRequest(
        GrammarGenerationRequestId id,
        UserId userId,
        Type type,
        String level,
        String targetLanguage,
        List<RuleSeed> rules,
        Instant createdAt
) {
    public GrammarGenerationRequest {
        rules = rules == null ? List.of() : List.copyOf(rules);
    }

    public enum Type {
        RULE_DRAFT,
        RULE_DETAILS
    }

    public record GrammarGenerationRequestId(String id) {}

    public record RuleSeed(String draftId, String identifier, String name) {}
}
