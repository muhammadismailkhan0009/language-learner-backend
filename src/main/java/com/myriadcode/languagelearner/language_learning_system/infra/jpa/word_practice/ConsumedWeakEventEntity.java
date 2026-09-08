package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "word_practice_consumed_weak_events")
@IdClass(ConsumedWeakEventEntity.Id.class)
class ConsumedWeakEventEntity {
    @jakarta.persistence.Id @Column(name = "user_id") private String userId;
    @jakarta.persistence.Id @Column(name = "weak_event_id", length = 1000) private String weakEventId;
    @Column(name = "vocabulary_id", nullable = false) private String vocabularyId;
    @Column(name = "consumed_at", nullable = false) private Instant consumedAt;
    protected ConsumedWeakEventEntity() {}
    ConsumedWeakEventEntity(String userId, String weakEventId, String vocabularyId, Instant consumedAt) {
        this.userId = userId; this.weakEventId = weakEventId; this.vocabularyId = vocabularyId; this.consumedAt = consumedAt;
    }
    public record Id(String userId, String weakEventId) implements Serializable {}
}
