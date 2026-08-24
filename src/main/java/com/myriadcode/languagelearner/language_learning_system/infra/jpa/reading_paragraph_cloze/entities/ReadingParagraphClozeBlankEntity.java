package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "reading_paragraph_cloze_blank")
public class ReadingParagraphClozeBlankEntity {
    @Id private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "paragraph_id", nullable = false)
    private ReadingParagraphClozeParagraphEntity paragraph;
    @Column(name = "blank_index", nullable = false) private int blankIndex;
    @Column(name = "blank_token", nullable = false) private String blankToken;
    @Column(name = "exact_answer", nullable = false, columnDefinition = "text") private String exactAnswer;
    @Column(name = "answer_explanation", nullable = false, columnDefinition = "text") private String answerExplanation;
    @Column(name = "practice_kind", nullable = false) private String practiceKind;
    @Column(name = "vocabulary_id") private String vocabularyId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reading_paragraph_cloze_blank_grammar_rule", joinColumns = @JoinColumn(name = "blank_id"))
    @Column(name = "grammar_rule_id", nullable = false)
    private Set<String> grammarRuleIds = new LinkedHashSet<>();

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public ReadingParagraphClozeParagraphEntity getParagraph() { return paragraph; }
    public void setParagraph(ReadingParagraphClozeParagraphEntity paragraph) { this.paragraph = paragraph; }
    public int getBlankIndex() { return blankIndex; } public void setBlankIndex(int blankIndex) { this.blankIndex = blankIndex; }
    public String getBlankToken() { return blankToken; } public void setBlankToken(String blankToken) { this.blankToken = blankToken; }
    public String getExactAnswer() { return exactAnswer; } public void setExactAnswer(String exactAnswer) { this.exactAnswer = exactAnswer; }
    public String getAnswerExplanation() { return answerExplanation; } public void setAnswerExplanation(String answerExplanation) { this.answerExplanation = answerExplanation; }
    public String getPracticeKind() { return practiceKind; } public void setPracticeKind(String practiceKind) { this.practiceKind = practiceKind; }
    public String getVocabularyId() { return vocabularyId; } public void setVocabularyId(String vocabularyId) { this.vocabularyId = vocabularyId; }
    public Set<String> getGrammarRuleIds() { return grammarRuleIds; }
    public void setGrammarRuleIds(Set<String> grammarRuleIds) { this.grammarRuleIds = grammarRuleIds == null ? new LinkedHashSet<>() : grammarRuleIds; }
}
