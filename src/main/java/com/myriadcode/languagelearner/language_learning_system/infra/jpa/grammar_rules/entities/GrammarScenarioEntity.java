package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grammar_scenarios")
public class GrammarScenarioEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(name = "target_language", nullable = false)
    private String targetLanguage;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "is_fixed", nullable = false)
    private boolean isFixed;

    @OneToMany(mappedBy = "grammarScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<GrammarScenarioSentenceEntity> sentences = new ArrayList<>();

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }

    public List<GrammarScenarioSentenceEntity> getSentences() {
        return sentences;
    }

    public void setSentences(List<GrammarScenarioSentenceEntity> sentences) {
        this.sentences.clear();
        if (sentences == null) {
            return;
        }
        for (GrammarScenarioSentenceEntity sentence : sentences) {
            addSentence(sentence);
        }
    }

    public void addSentence(GrammarScenarioSentenceEntity sentence) {
        sentence.setGrammarScenario(this);
        this.sentences.add(sentence);
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
