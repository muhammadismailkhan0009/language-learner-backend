package com.myriadcode.languagelearner.language_content.infra.jpa.entities;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
public class UserStatsForContentEntity {

    @Id
    private String id;

    @Column
    private String userId;

    @Column
    @Enumerated(EnumType.STRING)
    private GermanAdaptive.ScenarioEnum scenario;

    @Column
    @Enumerated(EnumType.STRING)
    private GermanAdaptive.CommunicativeFunctionEnum function;

    @Column
    @Enumerated(EnumType.STRING)
    private GermanAdaptive.GrammarRuleEnum grammarRule;

    @Column
    private LocalDateTime syllabusAssignedAt;

    public LocalDateTime getSyllabusAssignedAt() {
        return syllabusAssignedAt;
    }

    public void setSyllabusAssignedAt(LocalDateTime syllabusAssignedAt) {
        this.syllabusAssignedAt = syllabusAssignedAt;
    }

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

    public GermanAdaptive.ScenarioEnum getScenario() {
        return scenario;
    }

    public void setScenario(GermanAdaptive.ScenarioEnum scenario) {
        this.scenario = scenario;
    }

    public GermanAdaptive.CommunicativeFunctionEnum getFunction() {
        return function;
    }

    public void setFunction(GermanAdaptive.CommunicativeFunctionEnum function) {
        this.function = function;
    }

    public GermanAdaptive.GrammarRuleEnum getGrammarRule() {
        return grammarRule;
    }

    public void setGrammarRule(GermanAdaptive.GrammarRuleEnum grammarRule) {
        this.grammarRule = grammarRule;
    }
}
