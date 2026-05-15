package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "reading_paragraph_cloze_paragraph")
public class ReadingParagraphClozeParagraphEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingParagraphClozeSessionEntity session;

    @Column(name = "paragraph_index", nullable = false)
    private int paragraphIndex;

    @Column(name = "scenario_label", nullable = false)
    private String scenarioLabel;

    @Column(name = "cloze_paragraph", nullable = false, columnDefinition = "text")
    private String clozeParagraph;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "paragraph", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private Set<ReadingParagraphClozeCardEntity> cards = new LinkedHashSet<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ReadingParagraphClozeSessionEntity getSession() { return session; }
    public void setSession(ReadingParagraphClozeSessionEntity session) { this.session = session; }
    public int getParagraphIndex() { return paragraphIndex; }
    public void setParagraphIndex(int paragraphIndex) { this.paragraphIndex = paragraphIndex; }
    public String getScenarioLabel() { return scenarioLabel; }
    public void setScenarioLabel(String scenarioLabel) { this.scenarioLabel = scenarioLabel; }
    public String getClozeParagraph() { return clozeParagraph; }
    public void setClozeParagraph(String clozeParagraph) { this.clozeParagraph = clozeParagraph; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Set<ReadingParagraphClozeCardEntity> getCards() { return cards; }
    public void setCards(Set<ReadingParagraphClozeCardEntity> cards) { this.cards = cards; }
}

