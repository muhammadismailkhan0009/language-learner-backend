package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reading_practice_paragraph")
public class ReadingPracticeParagraphEntity {

    @Id
    private String id;

    @Column(name = "paragraph_text", nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "paragraph_id", nullable = false)
    @OrderColumn(name = "sentence_index")
    private List<ReadingPracticeSentenceEntity> sentences = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<ReadingPracticeSentenceEntity> getSentences() {
        return sentences;
    }

    public void setSentences(List<ReadingPracticeSentenceEntity> sentences) {
        this.sentences.clear();
        if (sentences == null) {
            return;
        }
        for (ReadingPracticeSentenceEntity sentence : sentences) {
            addSentence(sentence);
        }
    }

    public void addSentence(ReadingPracticeSentenceEntity sentence) {
        this.sentences.add(sentence);
    }
}
