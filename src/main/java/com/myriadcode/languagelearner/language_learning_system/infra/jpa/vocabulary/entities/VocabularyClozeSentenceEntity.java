package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "vocabulary_cloze_sentences")
public class VocabularyClozeSentenceEntity {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false, unique = true)
    private VocabularyEntity vocabulary;

    @Column(name = "cloze_text", nullable = false, length = 2000)
    private String clozeText;

    @Column(name = "hint", nullable = false, length = 500)
    private String hint;

    @Column(name = "answer_text", nullable = false, length = 500)
    private String answerText;

    @Lob
    @Column(name = "answer_words_json", nullable = false, columnDefinition = "text")
    private String answerWordsJson;

    @Column(name = "answer_translation", nullable = false, length = 500)
    private String answerTranslation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

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

    public String getClozeText() {
        return clozeText;
    }

    public void setClozeText(String clozeText) {
        this.clozeText = clozeText;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getAnswerWordsJson() {
        return answerWordsJson;
    }

    public void setAnswerWordsJson(String answerWordsJson) {
        this.answerWordsJson = answerWordsJson;
    }

    public String getAnswerTranslation() {
        return answerTranslation;
    }

    public void setAnswerTranslation(String answerTranslation) {
        this.answerTranslation = answerTranslation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
