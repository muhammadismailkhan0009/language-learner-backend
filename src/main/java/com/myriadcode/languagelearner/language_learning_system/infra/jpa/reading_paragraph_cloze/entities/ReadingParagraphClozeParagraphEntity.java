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

    @OneToMany(mappedBy = "paragraph", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("blankIndex ASC")
    private Set<ReadingParagraphClozeBlankEntity> blanks = new LinkedHashSet<>();

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
    public Set<ReadingParagraphClozeBlankEntity> getBlanks() { return blanks; }
    public void setBlanks(Set<ReadingParagraphClozeBlankEntity> blanks) {
        this.blanks.clear();
        if (blanks != null) blanks.forEach(this::addBlank);
    }
    public void addBlank(ReadingParagraphClozeBlankEntity blank) { blank.setParagraph(this); this.blanks.add(blank); }
}
