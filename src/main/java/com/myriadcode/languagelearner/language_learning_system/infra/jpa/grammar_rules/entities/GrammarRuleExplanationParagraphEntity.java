package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "grammar_rule_explanation_paragraphs")
public class GrammarRuleExplanationParagraphEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grammar_rule_id", nullable = false)
    private GrammarRuleEntity grammarRule;

    @Column(name = "paragraph_text", nullable = false, length = 5000)
    private String paragraphText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GrammarRuleEntity getGrammarRule() {
        return grammarRule;
    }

    public void setGrammarRule(GrammarRuleEntity grammarRule) {
        this.grammarRule = grammarRule;
    }

    public String getParagraphText() {
        return paragraphText;
    }

    public void setParagraphText(String paragraphText) {
        this.paragraphText = paragraphText;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
