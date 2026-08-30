package com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model;

public enum ContentGenerationJobType {
    VOCABULARY_CLOZE,
    READING_PARAGRAPH_CLOZE,
    READING_PRACTICE,
    WRITING_PRACTICE,
    GRAMMAR_RULE_DRAFT,
    GRAMMAR_RULE_DETAILS,
    GRAMMAR_LEVEL_REASSIGNMENT,
    @Deprecated(forRemoval = true)
    PRACTICE_VOCABULARY_EXTRACTION,
    VOCABULARY_CANDIDATE_EXTRACTION,
    VOCABULARY_DETAIL_GENERATION
}
