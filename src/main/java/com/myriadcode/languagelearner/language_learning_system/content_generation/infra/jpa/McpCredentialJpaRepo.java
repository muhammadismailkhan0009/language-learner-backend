package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface McpCredentialJpaRepo extends JpaRepository<McpCredentialEntity, String> {
    Optional<McpCredentialEntity> findBySecretKey(String secretKey);
}
