package com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.McpCredential;

import java.util.Optional;

public interface McpCredentialRepo {
    McpCredential save(McpCredential credential);
    Optional<McpCredential> findByUserId(String userId);
    Optional<McpCredential> findBySecretKey(String secretKey);
}
