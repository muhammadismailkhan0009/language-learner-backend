package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeSessionEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.mappers.ReadingPracticeJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.repos.ReadingPracticeSessionJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ReadingPracticeJpaRepoImpl implements ReadingPracticeRepo {

    private static final ReadingPracticeJpaMapper READING_PRACTICE_JPA_MAPPER = ReadingPracticeJpaMapper.INSTANCE;

    private final ReadingPracticeSessionJpaRepo readingPracticeSessionJpaRepo;

    public ReadingPracticeJpaRepoImpl(ReadingPracticeSessionJpaRepo readingPracticeSessionJpaRepo) {
        this.readingPracticeSessionJpaRepo = readingPracticeSessionJpaRepo;
    }

    @Override
    @Transactional
    public ReadingPracticeSession save(ReadingPracticeSession session) {
        var entity = READING_PRACTICE_JPA_MAPPER.toEntity(session);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }

        var usageEntities = session.vocabularyUsages() == null ? List.<ReadingVocabularyUsage>of() : session.vocabularyUsages();
        entity.setVocabularyUsages(usageEntities.stream()
                .map(READING_PRACTICE_JPA_MAPPER::toUsageEntity)
                .peek(usage -> {
                    if (usage.getCreatedAt() == null) {
                        usage.setCreatedAt(Instant.now());
                    }
                })
                .toList());

        var saved = readingPracticeSessionJpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReadingPracticeSession> findByIdAndUserId(String sessionId, String userId) {
        return readingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, userId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingPracticeSession> findAllByUserId(String userId) {
        return readingPracticeSessionJpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomainSummary)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(String sessionId, String userId) {
        readingPracticeSessionJpaRepo.deleteByIdAndUserId(sessionId, userId);
    }

    private ReadingPracticeSession toDomain(ReadingPracticeSessionEntity entity) {
        var base = READING_PRACTICE_JPA_MAPPER.toDomain(entity);
        var usages = entity.getVocabularyUsages().stream()
                .map(READING_PRACTICE_JPA_MAPPER::toUsageDomain)
                .toList();
        return new ReadingPracticeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.readingText(),
                base.createdAt(),
                usages
        );
    }

    private ReadingPracticeSession toDomainSummary(ReadingPracticeSessionEntity entity) {
        var base = READING_PRACTICE_JPA_MAPPER.toDomain(entity);
        var usages = entity.getVocabularyUsages().stream()
                .map(READING_PRACTICE_JPA_MAPPER::toUsageDomain)
                .toList();
        return new ReadingPracticeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.readingText(),
                base.createdAt(),
                usages
        );
    }
}
