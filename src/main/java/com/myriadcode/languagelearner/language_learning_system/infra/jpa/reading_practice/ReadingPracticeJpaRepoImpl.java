package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeSessionEntity;
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

        var usageEntities = session.vocabularyUsages() == null ? List.<ReadingVocabularyUsage>of() : session.vocabularyUsages();
        entity.setVocabularyUsages(new java.util.LinkedHashSet<>(usageEntities.stream()
                .map(READING_PRACTICE_JPA_MAPPER::toUsageEntity)
                .peek(usage -> {
                    if (usage.getCreatedAt() == null) {
                        usage.setCreatedAt(Instant.now());
                    }
                })
                .toList()));

        var paragraphEntities = session.paragraphs() == null ? List.<ReadingPracticeParagraph>of() : session.paragraphs();
        entity.setParagraphs(paragraphEntities.stream()
                .map(paragraph -> {
                    var paragraphEntity = READING_PRACTICE_JPA_MAPPER.toParagraphEntity(paragraph);
                    if (paragraphEntity.getCreatedAt() == null) {
                        paragraphEntity.setCreatedAt(Instant.now());
                    }
                    var sentenceEntities = paragraph.sentences() == null
                            ? List.<ReadingPracticeSentence>of()
                            : paragraph.sentences();
                    paragraphEntity.setSentences(sentenceEntities.stream()
                            .map(READING_PRACTICE_JPA_MAPPER::toSentenceEntity)
                            .peek(sentence -> {
                                if (sentence.getCreatedAt() == null) {
                                    sentence.setCreatedAt(Instant.now());
                                }
                            })
                            .toList());
                    return paragraphEntity;
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
    @Transactional(readOnly = true)
    public List<String> findRecentTopicsByUserId(String userId, int limit) {
        return readingPracticeSessionJpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(ReadingPracticeSessionEntity::getTopic)
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
                    var usages = session.getVocabularyUsages();
                    if (usages == null || usages.isEmpty()) {
                        return;
                    }
                    var removed = usages.removeIf(usage -> flashcardId.equals(usage.getFlashcardId()));
                    if (removed) {
                        readingPracticeSessionJpaRepo.save(session);
                    }
                });
    }

    private ReadingPracticeSession toDomain(ReadingPracticeSessionEntity entity) {
        var base = READING_PRACTICE_JPA_MAPPER.toDomain(entity);
        var usages = entity.getVocabularyUsages().stream()
                .map(READING_PRACTICE_JPA_MAPPER::toUsageDomain)
                .toList();
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
        return new ReadingPracticeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.readingText(),
                paragraphs,
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
                List.of(),
                base.createdAt(),
                usages
        );
    }
}
