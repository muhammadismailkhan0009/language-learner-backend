package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "universal_vocabulary_pool",
        uniqueConstraints = @UniqueConstraint(name = "uk_universal_vocab_surface_kind", columnNames = {"normalized_surface", "entry_kind"}))
public class UniversalVocabularyPoolEntity {

    @Id
    private String id;

    @Column(name = "normalized_surface", nullable = false)
    private String normalizedSurface;

    @Column(name = "surface", nullable = false)
    private String surface;

    @Column(name = "entry_kind", nullable = false)
    private String entryKind;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        var now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNormalizedSurface() { return normalizedSurface; }
    public void setNormalizedSurface(String normalizedSurface) { this.normalizedSurface = normalizedSurface; }
    public String getSurface() { return surface; }
    public void setSurface(String surface) { this.surface = surface; }
    public String getEntryKind() { return entryKind; }
    public void setEntryKind(String entryKind) { this.entryKind = entryKind; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
