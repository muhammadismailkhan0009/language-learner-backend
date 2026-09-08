package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;

interface ConsumedWeakEventJpaRepository extends JpaRepository<ConsumedWeakEventEntity, ConsumedWeakEventEntity.Id> {
    interface WeakEventIdView { String getWeakEventId(); }
    Set<WeakEventIdView> findAllByUserId(String userId);
}
