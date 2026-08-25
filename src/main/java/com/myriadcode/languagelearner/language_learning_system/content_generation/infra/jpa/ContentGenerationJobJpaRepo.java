package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

interface ContentGenerationJobJpaRepo extends JpaRepository<ContentGenerationJobEntity, String> {
}
