package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vocabulary_example_sentences")
public class VocabularyExampleSentenceEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private VocabularyEntity vocabulary;

    @Column(nullable = false)
    private String sentence;

    @Column(nullable = false)
    private String translation;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VocabularyEntity getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(VocabularyEntity vocabulary) {
        this.vocabulary = vocabulary;
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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
