package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "grammar_generation_requests")
public class GrammarGenerationRequestEntity {
    @Id private String id;
    @Column(name = "user_id", nullable = false) private String userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private GrammarGenerationRequest.Type type;
    @Column(nullable = false) private String level;
    @Column(name = "target_language", nullable = false) private String targetLanguage;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false) private List<RuleSeedData> rules;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected GrammarGenerationRequestEntity() {}

    public GrammarGenerationRequestEntity(String id, String userId, GrammarGenerationRequest.Type type,
                                          String level, String targetLanguage, List<RuleSeedData> rules,
                                          Instant createdAt) {
        this.id = id; this.userId = userId; this.type = type; this.level = level;
        this.targetLanguage = targetLanguage; this.rules = rules; this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public GrammarGenerationRequest.Type getType() { return type; }
    public String getLevel() { return level; }
    public String getTargetLanguage() { return targetLanguage; }
    public List<RuleSeedData> getRules() { return rules; }
    public Instant getCreatedAt() { return createdAt; }

    public record RuleSeedData(String draftId, String identifier, String name) {}
}
