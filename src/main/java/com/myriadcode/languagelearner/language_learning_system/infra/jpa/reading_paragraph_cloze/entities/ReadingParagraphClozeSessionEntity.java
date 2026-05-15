package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "reading_paragraph_cloze_session")
public class ReadingParagraphClozeSessionEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "cloze_paragraph", nullable = false, columnDefinition = "text")
    private String clozeParagraph;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Fetch(FetchMode.SUBSELECT)
    private Set<ReadingParagraphClozeCardEntity> cards = new LinkedHashSet<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getClozeParagraph() { return clozeParagraph; }
    public void setClozeParagraph(String clozeParagraph) { this.clozeParagraph = clozeParagraph; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Set<ReadingParagraphClozeCardEntity> getCards() { return cards; }
    public void setCards(Set<ReadingParagraphClozeCardEntity> cards) {
        this.cards.clear();
        if (cards == null) return;
        cards.forEach(this::addCard);
    }
    public void addCard(ReadingParagraphClozeCardEntity card) {
        card.setSession(this);
        this.cards.add(card);
    }
}
