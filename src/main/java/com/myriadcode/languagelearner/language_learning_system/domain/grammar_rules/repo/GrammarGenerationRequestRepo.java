package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;

import java.util.Optional;

public interface GrammarGenerationRequestRepo {
    GrammarGenerationRequest save(GrammarGenerationRequest request);
    Optional<GrammarGenerationRequest> findOldestByUserIdAndType(String userId, GrammarGenerationRequest.Type type);
    boolean existsByUserIdAndType(String userId, GrammarGenerationRequest.Type type);
    void delete(GrammarGenerationRequest.GrammarGenerationRequestId id);
}
