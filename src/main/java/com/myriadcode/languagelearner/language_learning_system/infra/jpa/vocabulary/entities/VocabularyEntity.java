package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "vocabulary_entries")
public class VocabularyEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String surface;

    @Column(nullable = false)
    private String translation;

    @Column(name = "entry_kind", nullable = false)
    private String entryKind;

    @Column(name = "notes", length = 5000)
    private String notes;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion = 1;

    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<VocabularyExampleSentenceEntity> exampleSentences = new ArrayList<>();

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

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getEntryKind() {
        return entryKind;
    }

    public void setEntryKind(String entryKind) {
        this.entryKind = entryKind;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<VocabularyExampleSentenceEntity> getExampleSentences() {
        return exampleSentences;
    }

    public void setExampleSentences(List<VocabularyExampleSentenceEntity> exampleSentences) {
        this.exampleSentences.clear();
        if (exampleSentences == null) {
            return;
        }
        for (VocabularyExampleSentenceEntity sentence : exampleSentences) {
            addExampleSentence(sentence);
        }
    }

    public void addExampleSentence(VocabularyExampleSentenceEntity sentence) {
        sentence.setVocabulary(this);
        this.exampleSentences.add(sentence);
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
