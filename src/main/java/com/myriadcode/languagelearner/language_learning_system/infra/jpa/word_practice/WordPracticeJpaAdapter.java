package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Repository
class WordPracticeJpaAdapter implements WordPracticeRepo {
    private final WordPracticeJpaRepository practices;
    private final ConsumedWeakEventJpaRepository weakEvents;

    WordPracticeJpaAdapter(WordPracticeJpaRepository practices, ConsumedWeakEventJpaRepository weakEvents) {
        this.practices = practices; this.weakEvents = weakEvents;
    }

    public int countDistinctActiveVocabulary(String userId) {
        return Math.toIntExact(practices.countDistinctVocabularyByUserId(userId));
    }
    public Set<String> findActiveVocabularyIds(String userId) { return Set.copyOf(practices.findActiveVocabularyIds(userId)); }
    public List<WordPractice> findByUserId(String userId) {
        return practices.findAllByUserIdOrderByCreatedAtAscIdAsc(userId).stream().map(this::toDomain).toList();
    }
    public Optional<WordPractice> findByIdAndUserId(String practiceId, String userId) {
        return practices.findByIdAndUserId(practiceId, userId).map(this::toDomain);
    }
    @Transactional
    public void deleteByIdAndUserId(String practiceId, String userId) { practices.deleteByIdAndUserId(practiceId, userId); }

    @Transactional
    public void saveGenerationAndConsumeWeakEvents(String userId, List<WordPracticeCandidate> selected,
                                                    List<GeneratedWordPracticeGroup> groups, Instant createdAt) {
        var rows = groups.stream().flatMap(group -> group.practices().stream()).map(generated ->
                new WordPracticeEntity(UUID.randomUUID().toString(), userId, generated.vocabularyId(), generated.direction(),
                        generated.sourceSentence(), generated.clozeSentence(), generated.completeSentence(),
                        generated.exactAnswer(), generated.acceptedAnswers(), createdAt)).toList();
        practices.saveAll(rows);
        for (var candidate : selected) {
            if (candidate.category() == WordPracticeSelectionCategory.WEAK) {
                weakEvents.save(new ConsumedWeakEventEntity(userId, candidate.weakEventId(),
                        candidate.vocabularyId(), createdAt));
            }
        }
    }

    private WordPractice toDomain(WordPracticeEntity entity) {
        return new WordPractice(entity.id(), entity.userId(), entity.vocabularyId(), entity.direction(),
                entity.sourceSentence(), entity.clozeSentence(), entity.completeSentence(), entity.exactAnswer(),
                entity.acceptedAnswers(), entity.createdAt());
    }
}
