package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import org.springframework.stereotype.Service;

@Service
public class GrammarContentAuthorizationService {

    public void requireAuthenticatedUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user is required");
        }
    }
}
