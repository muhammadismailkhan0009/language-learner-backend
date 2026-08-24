package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "reading_paragraph_cloze_session")
public class ReadingParagraphClozeSessionEntity implements Persistable<String> {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "learner_level", nullable = false)
    private String learnerLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    private boolean isNew = true;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("paragraphIndex ASC")
    @Fetch(FetchMode.SUBSELECT)
    private Set<ReadingParagraphClozeParagraphEntity> paragraphs = new LinkedHashSet<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PostPersist
    @PostLoad
    public void markNotNew() {
        this.isNew = false;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    @Override
    public boolean isNew() { return isNew; }
    public void markExisting() { this.isNew = false; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLearnerLevel() { return learnerLevel; }
    public void setLearnerLevel(String learnerLevel) { this.learnerLevel = learnerLevel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Set<ReadingParagraphClozeParagraphEntity> getParagraphs() { return paragraphs; }
    public void setParagraphs(Set<ReadingParagraphClozeParagraphEntity> paragraphs) {
        this.paragraphs.clear();
        if (paragraphs == null) return;
        paragraphs.forEach(this::addParagraph);
    }
    public void addParagraph(ReadingParagraphClozeParagraphEntity paragraph) {
        paragraph.setSession(this);
        this.paragraphs.add(paragraph);
    }
}
