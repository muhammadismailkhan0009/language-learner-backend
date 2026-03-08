package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeSessionEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.mappers.WritingPracticeJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos.WritingPracticeSessionJpaRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class WritingPracticeJpaRepoImpl implements WritingPracticeRepo {

    private static final WritingPracticeJpaMapper WRITING_PRACTICE_JPA_MAPPER = WritingPracticeJpaMapper.INSTANCE;

    private final WritingPracticeSessionJpaRepo writingPracticeSessionJpaRepo;

    public WritingPracticeJpaRepoImpl(WritingPracticeSessionJpaRepo writingPracticeSessionJpaRepo) {
        this.writingPracticeSessionJpaRepo = writingPracticeSessionJpaRepo;
    }

    @Override
    @Transactional
    public WritingPracticeSession save(WritingPracticeSession session) {
        var entity = WRITING_PRACTICE_JPA_MAPPER.toEntity(session);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }

        var sentenceEntities = session.sentencePairs() == null ? List.<WritingSentencePair>of() : session.sentencePairs();
        entity.setSentencePairs(new LinkedHashSet<>(sentenceEntities.stream()
                .sorted(Comparator.comparingInt(WritingSentencePair::position))
                .map(WRITING_PRACTICE_JPA_MAPPER::toSentencePairEntity)
                .peek(pair -> {
                    if (pair.getCreatedAt() == null) {
                        pair.setCreatedAt(Instant.now());
                    }
                })
                .toList()));

        var usageEntities = session.vocabularyUsages() == null ? List.<WritingVocabularyUsage>of() : session.vocabularyUsages();
        entity.setVocabularyUsages(new LinkedHashSet<>(usageEntities.stream()
                .map(WRITING_PRACTICE_JPA_MAPPER::toUsageEntity)
                .peek(usage -> {
                    if (usage.getCreatedAt() == null) {
                        usage.setCreatedAt(Instant.now());
                    }
                })
                .toList()));

        return toDomain(writingPracticeSessionJpaRepo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WritingPracticeSession> findByIdAndUserId(String sessionId, String userId) {
        return writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, userId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WritingPracticeSession> findAllByUserId(String userId) {
        return writingPracticeSessionJpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomainSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findRecentTopicsByUserId(String userId, int limit) {
        return writingPracticeSessionJpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(WritingPracticeSessionEntity::getTopic)
                .toList();
    }

    @Override
    @Transactional
    public WritingPracticeSession updateSubmission(String sessionId,
                                                   String userId,
                                                   String submittedAnswer,
                                                   Instant submittedAt) {
        var entity = writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Writing session not found"));
        entity.setSubmittedAnswer(submittedAnswer);
        entity.setSubmittedAt(submittedAt);
        return toDomain(writingPracticeSessionJpaRepo.save(entity));
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(String sessionId, String userId) {
        writingPracticeSessionJpaRepo.deleteByIdAndUserId(sessionId, userId);
    }

    @Override
    @Transactional
    public void detachFlashcard(String userId, String sessionId, String flashcardId) {
        writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, userId)
                .ifPresent(session -> {
                    var usages = session.getVocabularyUsages();
                    if (usages == null || usages.isEmpty()) {
                        return;
                    }
                    var removed = usages.removeIf(usage -> flashcardId.equals(usage.getFlashcardId()));
                    if (removed) {
                        writingPracticeSessionJpaRepo.save(session);
                    }
                });
    }

    private WritingPracticeSession toDomain(WritingPracticeSessionEntity entity) {
        var base = WRITING_PRACTICE_JPA_MAPPER.toDomain(entity);
        var sentencePairs = entity.getSentencePairs().stream()
                .sorted(Comparator.comparingInt(com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeSentencePairEntity::getPosition))
                .map(WRITING_PRACTICE_JPA_MAPPER::toSentencePairDomain)
                .toList();
        var usages = entity.getVocabularyUsages().stream()
                .map(WRITING_PRACTICE_JPA_MAPPER::toUsageDomain)
                .toList();
        return new WritingPracticeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.englishParagraph(),
                base.germanParagraph(),
                base.createdAt(),
                base.submittedAnswer(),
                base.submittedAt(),
                sentencePairs,
                usages
        );
    }

    private WritingPracticeSession toDomainSummary(WritingPracticeSessionEntity entity) {
        var base = WRITING_PRACTICE_JPA_MAPPER.toDomain(entity);
        var usages = entity.getVocabularyUsages().stream()
                .map(WRITING_PRACTICE_JPA_MAPPER::toUsageDomain)
                .toList();
        return new WritingPracticeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.englishParagraph(),
                base.germanParagraph(),
                base.createdAt(),
                base.submittedAnswer(),
                base.submittedAt(),
                List.of(),
                usages
        );
    }
}
