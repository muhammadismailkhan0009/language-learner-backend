package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeSessionEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeScenarioEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.mappers.ReadingPracticeJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.repos.ReadingPracticeSessionJpaRepo;
import org.springframework.data.domain.PageRequest;
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

        entity.setScenarios(session.scenarios().stream().map(this::toScenarioEntity).toList());

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
    @Transactional(readOnly = true)
    public List<String> findRecentTopicsByUserId(String userId, int limit) {
        return readingPracticeSessionJpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(ReadingPracticeSessionEntity::getTopic)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<java.util.Set<String>> findRecentVocabularyUsageSessionSetsByUserId(String userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return readingPracticeSessionJpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(session -> session.getScenarios().stream()
                        .flatMap(scenario -> scenario.getVocabularyUsages().stream())
                        .map(com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeVocabularyUsageEntity::getVocabularyId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()))
                .toList();
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(String sessionId, String userId) {
        readingPracticeSessionJpaRepo.deleteByIdAndUserId(sessionId, userId);
    }

    @Override
    @Transactional
    public void detachFlashcard(String userId, String sessionId, String flashcardId) {
        readingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, userId)
                .ifPresent(session -> {
                    var removed = session.getScenarios().stream()
                            .map(ReadingPracticeScenarioEntity::getVocabularyUsages)
                            .map(usages -> usages.removeIf(usage -> flashcardId.equals(usage.getFlashcardId())))
                            .reduce(false, Boolean::logicalOr);
                    if (removed) {
                        readingPracticeSessionJpaRepo.save(session);
                    }
                });
    }

    private ReadingPracticeSession toDomain(ReadingPracticeSessionEntity entity) {
        var base = READING_PRACTICE_JPA_MAPPER.toDomain(entity);
        var scenarios = java.util.stream.IntStream.range(0, entity.getScenarios().size())
                .mapToObj(index -> toScenarioDomain(entity.getScenarios().get(index), index)).toList();
        var first = scenarios.isEmpty() ? null : scenarios.getFirst();
        return new ReadingPracticeSession(base.id(), base.userId(), base.topic(), base.readingText(),
                first == null ? List.of() : first.paragraphs(), base.createdAt(),
                first == null ? List.of() : first.vocabularyUsages(), scenarios);
    }

    private ReadingPracticeScenario toScenarioDomain(ReadingPracticeScenarioEntity entity, int position) {
        var paragraphs = entity.getParagraphs() == null ? List.<ReadingPracticeParagraph>of()
                : java.util.stream.IntStream.range(0, entity.getParagraphs().size())
                .mapToObj(index -> {
                    var paragraphEntity = entity.getParagraphs().get(index);
                    var baseParagraph = READING_PRACTICE_JPA_MAPPER.toParagraphDomain(paragraphEntity);
                    var sentences = paragraphEntity.getSentences() == null ? List.<ReadingPracticeSentence>of()
                            : java.util.stream.IntStream.range(0, paragraphEntity.getSentences().size())
                            .mapToObj(sentenceIndex -> {
                                var sentenceEntity = paragraphEntity.getSentences().get(sentenceIndex);
                                var baseSentence = READING_PRACTICE_JPA_MAPPER.toSentenceDomain(sentenceEntity);
                                return new ReadingPracticeSentence(
                                        baseSentence.id(),
                                        baseSentence.text(),
                                        sentenceIndex
                                );
                            })
                            .toList();
                    return new ReadingPracticeParagraph(
                            baseParagraph.id(),
                            baseParagraph.text(),
                            index,
                            sentences
                    );
                })
                .toList();
        var usages = entity.getVocabularyUsages().stream().map(READING_PRACTICE_JPA_MAPPER::toUsageDomain).toList();
        return new ReadingPracticeScenario(new ReadingPracticeScenario.ReadingPracticeScenarioId(entity.getId()),
                entity.getLabel(), entity.getReadingText(), position, paragraphs, usages);
    }

    private ReadingPracticeSession toDomainSummary(ReadingPracticeSessionEntity entity) {
        var base = READING_PRACTICE_JPA_MAPPER.toDomain(entity);
        return new ReadingPracticeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.readingText(),
                List.of(),
                base.createdAt(),
                List.of(),
                List.of()
        );
    }

    private ReadingPracticeScenarioEntity toScenarioEntity(ReadingPracticeScenario scenario) {
        var entity = new ReadingPracticeScenarioEntity();
        entity.setId(scenario.id().id()); entity.setLabel(scenario.label());
        entity.setReadingText(scenario.readingText()); entity.setCreatedAt(Instant.now());
        entity.setParagraphs(scenario.paragraphs().stream().map(paragraph -> {
            var p = READING_PRACTICE_JPA_MAPPER.toParagraphEntity(paragraph); p.setCreatedAt(Instant.now());
            p.setSentences(paragraph.sentences().stream().map(sentence -> {
                var s = READING_PRACTICE_JPA_MAPPER.toSentenceEntity(sentence); s.setCreatedAt(Instant.now()); return s;
            }).toList()); return p;
        }).toList());
        entity.setVocabularyUsages(new java.util.LinkedHashSet<>(scenario.vocabularyUsages().stream().map(usage -> {
            var u = READING_PRACTICE_JPA_MAPPER.toUsageEntity(usage); u.setCreatedAt(Instant.now()); return u;
        }).toList()));
        return entity;
    }
}
