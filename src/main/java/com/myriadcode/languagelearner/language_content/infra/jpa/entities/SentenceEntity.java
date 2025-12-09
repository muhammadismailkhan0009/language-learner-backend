package com.myriadcode.languagelearner.language_content.infra.jpa.entities;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import jakarta.persistence.*;

@Entity
@Table(name = "sentences",
        indexes = {
                @Index(columnList = "scenario, grammarRule, communicationFunction")
        })
public class SentenceEntity {

    @Id
    private String id;

    @Column
    private String sentence;

    @Column
    private String translation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GermanAdaptive.ScenarioEnum scenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GermanAdaptive.GrammarRuleEnum grammarRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GermanAdaptive.CommunicativeFunctionEnum communicationFunction;

    public GermanAdaptive.ScenarioEnum getScenario() {
        return scenario;
    }

    public void setScenario(GermanAdaptive.ScenarioEnum scenario) {
        this.scenario = scenario;
    }

    public GermanAdaptive.GrammarRuleEnum getGrammarRule() {
        return grammarRule;
    }

    public void setGrammarRule(GermanAdaptive.GrammarRuleEnum grammarRule) {
        this.grammarRule = grammarRule;
    }

    public GermanAdaptive.CommunicativeFunctionEnum getCommunicationFunction() {
        return communicationFunction;
    }

    public void setCommunicationFunction(GermanAdaptive.CommunicativeFunctionEnum communicationFunction) {
        this.communicationFunction = communicationFunction;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
