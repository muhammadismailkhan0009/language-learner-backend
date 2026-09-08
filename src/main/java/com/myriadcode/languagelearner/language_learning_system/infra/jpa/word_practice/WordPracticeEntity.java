package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "word_practices")
class WordPracticeEntity {
    @Id private String id;
    @Column(name = "user_id", nullable = false) private String userId;
    @Column(name = "vocabulary_id", nullable = false) private String vocabularyId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private WordPracticeDirection direction;
    @Column(name = "source_sentence", nullable = false) private String sourceSentence;
    @Column(name = "cloze_sentence", nullable = false) private String clozeSentence;
    @Column(name = "complete_sentence", nullable = false) private String completeSentence;
    @Column(name = "exact_answer", nullable = false) private String exactAnswer;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "accepted_answers", columnDefinition = "jsonb", nullable = false)
    private List<String> acceptedAnswers;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected WordPracticeEntity() {}

    WordPracticeEntity(String id, String userId, String vocabularyId, WordPracticeDirection direction,
                       String sourceSentence, String clozeSentence, String completeSentence, String exactAnswer,
                       List<String> acceptedAnswers, Instant createdAt) {
        this.id = id; this.userId = userId; this.vocabularyId = vocabularyId; this.direction = direction;
        this.sourceSentence = sourceSentence; this.clozeSentence = clozeSentence;
        this.completeSentence = completeSentence; this.exactAnswer = exactAnswer;
        this.acceptedAnswers = List.copyOf(acceptedAnswers); this.createdAt = createdAt;
    }

    String id() { return id; }
    String userId() { return userId; }
    String vocabularyId() { return vocabularyId; }
    WordPracticeDirection direction() { return direction; }
    String sourceSentence() { return sourceSentence; }
    String clozeSentence() { return clozeSentence; }
    String completeSentence() { return completeSentence; }
    String exactAnswer() { return exactAnswer; }
    List<String> acceptedAnswers() { return acceptedAnswers; }
    Instant createdAt() { return createdAt; }
}
