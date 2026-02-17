package com.myriadcode.languagelearner.language_learning_system.scenarios.infra.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenarios")
public class ScenarioEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String nature;

    @Column(name = "target_language", nullable = false)
    private String targetLanguage;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ScenarioSentenceEntity> sentences = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public List<ScenarioSentenceEntity> getSentences() {
        return sentences;
    }

    public void setSentences(List<ScenarioSentenceEntity> sentences) {
        this.sentences.clear();
        if (sentences == null) {
            return;
        }
        for (ScenarioSentenceEntity sentence : sentences) {
            addSentence(sentence);
        }
    }

    public void addSentence(ScenarioSentenceEntity sentence) {
        sentence.setScenario(this);
        this.sentences.add(sentence);
    }
}
