package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grammar_rules")
public class GrammarRuleEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "grammar_scenario_id", nullable = false, unique = true)
    private GrammarScenarioEntity grammarScenario;

    @OneToMany(mappedBy = "grammarRule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<GrammarRuleExplanationParagraphEntity> explanationParagraphs = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GrammarScenarioEntity getGrammarScenario() {
        return grammarScenario;
    }

    public void setGrammarScenario(GrammarScenarioEntity grammarScenario) {
        this.grammarScenario = grammarScenario;
    }

    public List<GrammarRuleExplanationParagraphEntity> getExplanationParagraphs() {
        return explanationParagraphs;
    }

    public void setExplanationParagraphs(List<GrammarRuleExplanationParagraphEntity> explanationParagraphs) {
        this.explanationParagraphs.clear();
        if (explanationParagraphs == null) {
            return;
        }
        for (GrammarRuleExplanationParagraphEntity paragraph : explanationParagraphs) {
            addExplanationParagraph(paragraph);
        }
    }

    public void addExplanationParagraph(GrammarRuleExplanationParagraphEntity paragraph) {
        paragraph.setGrammarRule(this);
        this.explanationParagraphs.add(paragraph);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
