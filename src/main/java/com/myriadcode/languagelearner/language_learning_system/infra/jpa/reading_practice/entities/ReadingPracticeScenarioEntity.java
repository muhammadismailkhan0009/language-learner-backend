package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "reading_practice_scenario")
public class ReadingPracticeScenarioEntity {
    @Id private String id;
    @Column(name = "scenario_label", nullable = false) private String label;
    @Column(name = "reading_text", nullable = false, columnDefinition = "text") private String readingText;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "rated_cards_count", nullable = false) private int ratedCardsCount;
    @Column(name = "all_cards_rated", nullable = false) private boolean allCardsRated;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "scenario_id", nullable = false)
    @OrderColumn(name = "paragraph_index")
    private List<ReadingPracticeParagraphEntity> paragraphs = new ArrayList<>();
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private Set<ReadingPracticeVocabularyUsageEntity> vocabularyUsages = new LinkedHashSet<>();

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getLabel() { return label; } public void setLabel(String label) { this.label = label; }
    public String getReadingText() { return readingText; } public void setReadingText(String readingText) { this.readingText = readingText; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public int getRatedCardsCount() { return ratedCardsCount; } public void setRatedCardsCount(int value) { this.ratedCardsCount = value; }
    public boolean isAllCardsRated() { return allCardsRated; } public void setAllCardsRated(boolean value) { this.allCardsRated = value; }
    public List<ReadingPracticeParagraphEntity> getParagraphs() { return paragraphs; }
    public void setParagraphs(List<ReadingPracticeParagraphEntity> values) { paragraphs.clear(); if (values != null) paragraphs.addAll(values); }
    public Set<ReadingPracticeVocabularyUsageEntity> getVocabularyUsages() { return vocabularyUsages; }
    public void setVocabularyUsages(Set<ReadingPracticeVocabularyUsageEntity> values) {
        vocabularyUsages.clear(); if (values != null) values.forEach(this::addVocabularyUsage);
    }
    public void addVocabularyUsage(ReadingPracticeVocabularyUsageEntity usage) { usage.setScenario(this); vocabularyUsages.add(usage); }
}
