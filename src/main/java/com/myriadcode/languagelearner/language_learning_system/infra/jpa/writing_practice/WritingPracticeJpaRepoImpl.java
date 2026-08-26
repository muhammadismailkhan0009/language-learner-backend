package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingGrammarIssueAnalytics;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingStructuredFeedback;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingGrammarIssueAnalyticsEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeSessionEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeScenarioEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.mappers.WritingPracticeJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos.WritingGrammarIssueAnalyticsJpaRepo;
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
    private final WritingGrammarIssueAnalyticsJpaRepo analyticsJpaRepo;
    private final ObjectMapper objectMapper;

    public WritingPracticeJpaRepoImpl(WritingPracticeSessionJpaRepo writingPracticeSessionJpaRepo,
                                      WritingGrammarIssueAnalyticsJpaRepo analyticsJpaRepo,
                                      ObjectMapper objectMapper) {
        this.writingPracticeSessionJpaRepo = writingPracticeSessionJpaRepo;
        this.analyticsJpaRepo = analyticsJpaRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public WritingPracticeSession save(WritingPracticeSession session) {
        var entity = WRITING_PRACTICE_JPA_MAPPER.toEntity(session);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        if (session.scenarios().isEmpty()) throw new IllegalArgumentException("Writing session requires scenarios");
        var first = session.scenarios().getFirst();
        entity.setTopic(first.topic()); entity.setEnglishParagraph(first.englishParagraph()); entity.setGermanParagraph(first.germanParagraph());
        entity.setScenarios(session.scenarios().stream().map(scenario -> toScenarioEntity(entity, scenario)).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));

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
                .flatMap(session -> session.getScenarios().stream())
                .sorted(Comparator.comparingInt(WritingPracticeScenarioEntity::getPosition))
                .map(WritingPracticeScenarioEntity::getTopic)
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<java.util.Set<String>> findRecentVocabularyUsageSessionSetsByUserId(String userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return writingPracticeSessionJpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(session -> session.getScenarios().stream().flatMap(scenario -> scenario.getVocabularyUsages().stream())
                        .map(com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeVocabularyUsageEntity::getVocabularyId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()))
                .toList();
    }

    @Override
    @Transactional
    public WritingPracticeSession updateSubmission(String sessionId,
                                                   String scenarioId,
                                                   String userId,
                                                   String submittedAnswer,
                                                   Instant submittedAt,
                                                   String feedbackText,
                                                   WritingStructuredFeedback structuredFeedback,
                                                   Instant feedbackGeneratedAt) {
        var entity = writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Writing session not found"));
        var scenario = entity.getScenarios().stream().filter(value -> value.getId().equals(scenarioId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Writing scenario not found"));
        scenario.setSubmittedAnswer(submittedAnswer); scenario.setSubmittedAt(submittedAt);
        scenario.setFeedbackText(feedbackText); scenario.setStructuredFeedbackJson(toJson(structuredFeedback));
        scenario.setFeedbackGeneratedAt(feedbackGeneratedAt);
        return toDomain(writingPracticeSessionJpaRepo.save(entity));
    }

    @Override
    @Transactional
    public void saveGrammarIssueAnalytics(List<WritingGrammarIssueAnalytics> analytics) {
        if (analytics == null || analytics.isEmpty()) {
            return;
        }
        analyticsJpaRepo.saveAll(analytics.stream()
                .map(this::toAnalyticsEntity)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WritingGrammarIssueAnalytics> findGrammarIssueAnalytics(String sessionId, String userId) {
        return analyticsJpaRepo.findAllBySessionIdAndUserIdOrderByPriorityDescCreatedAtAsc(sessionId, userId).stream()
                .map(this::toAnalyticsDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteGrammarIssueAnalytics(String sessionId, String userId) {
        analyticsJpaRepo.deleteBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    @Transactional
    public void deleteGrammarIssueAnalytics(String sessionId, String scenarioId, String userId) {
        analyticsJpaRepo.deleteBySessionIdAndScenarioIdAndUserId(sessionId, scenarioId, userId);
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(String sessionId, String userId) {
        writingPracticeSessionJpaRepo.deleteByIdAndUserId(sessionId, userId);
    }

    @Override
    @Transactional
    public void detachFlashcard(String userId, String sessionId, String scenarioId, String flashcardId) {
        writingPracticeSessionJpaRepo.findByIdAndUserId(sessionId, userId)
                .ifPresent(session -> {
                    var scenario = session.getScenarios().stream().filter(value -> value.getId().equals(scenarioId)).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Writing scenario not found"));
                    var usages = scenario.getVocabularyUsages();
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
        return new WritingPracticeSession(base.id(), base.userId(), base.createdAt(), entity.getScenarios().stream()
                .sorted(Comparator.comparingInt(WritingPracticeScenarioEntity::getPosition)).map(this::toScenarioDomain).toList());
    }

    private WritingPracticeSession toDomainSummary(WritingPracticeSessionEntity entity) {
        var base = WRITING_PRACTICE_JPA_MAPPER.toDomain(entity);
        return new WritingPracticeSession(base.id(), base.userId(), base.createdAt(), entity.getScenarios().stream()
                .sorted(Comparator.comparingInt(WritingPracticeScenarioEntity::getPosition)).map(this::toScenarioDomain).toList());
    }

    private WritingPracticeScenarioEntity toScenarioEntity(WritingPracticeSessionEntity session, WritingPracticeScenario scenario) {
        var entity = new WritingPracticeScenarioEntity();
        entity.setId(scenario.id().id()); entity.setSession(session); entity.setPosition(scenario.position());
        entity.setTopic(scenario.topic()); entity.setEnglishParagraph(scenario.englishParagraph()); entity.setGermanParagraph(scenario.germanParagraph());
        entity.setSubmittedAnswer(scenario.submittedAnswer()); entity.setSubmittedAt(scenario.submittedAt()); entity.setFeedbackText(scenario.feedbackText());
        entity.setStructuredFeedbackJson(toJson(scenario.structuredFeedback())); entity.setFeedbackGeneratedAt(scenario.feedbackGeneratedAt()); entity.setCreatedAt(Instant.now());
        scenario.sentencePairs().stream().sorted(Comparator.comparingInt(WritingSentencePair::position)).map(WRITING_PRACTICE_JPA_MAPPER::toSentencePairEntity)
                .peek(value -> value.setCreatedAt(Instant.now())).forEach(entity::addSentencePair);
        scenario.vocabularyUsages().stream().map(WRITING_PRACTICE_JPA_MAPPER::toUsageEntity)
                .peek(value -> value.setCreatedAt(Instant.now())).forEach(entity::addVocabularyUsage);
        return entity;
    }

    private WritingPracticeScenario toScenarioDomain(WritingPracticeScenarioEntity entity) {
        return new WritingPracticeScenario(new WritingPracticeScenario.WritingPracticeScenarioId(entity.getId()), entity.getPosition(),
                entity.getTopic(), entity.getEnglishParagraph(), entity.getGermanParagraph(), entity.getSubmittedAnswer(), entity.getSubmittedAt(),
                entity.getFeedbackText(), fromJson(entity.getStructuredFeedbackJson()), entity.getFeedbackGeneratedAt(),
                entity.getSentencePairs().stream().sorted(Comparator.comparingInt(value -> value.getPosition())).map(WRITING_PRACTICE_JPA_MAPPER::toSentencePairDomain).toList(),
                entity.getVocabularyUsages().stream().map(WRITING_PRACTICE_JPA_MAPPER::toUsageDomain).toList());
    }

    private WritingGrammarIssueAnalyticsEntity toAnalyticsEntity(WritingGrammarIssueAnalytics analytics) {
        var entity = new WritingGrammarIssueAnalyticsEntity();
        entity.setId(analytics.id().id());
        entity.setSessionId(analytics.sessionId().id());
        entity.setScenarioId(analytics.scenarioId().id());
        entity.setUserId(analytics.userId().id());
        entity.setGrammarRuleIdentifier(analytics.grammarRuleIdentifier());
        entity.setIssueType(analytics.issueType());
        entity.setPriority(analytics.priority());
        entity.setLearnerText(analytics.learnerText());
        entity.setCorrectedText(analytics.correctedText());
        entity.setShortExplanation(analytics.shortExplanation());
        entity.setOccurrenceCount(analytics.occurrenceCount());
        entity.setCreatedAt(analytics.createdAt() == null ? Instant.now() : analytics.createdAt());
        return entity;
    }

    private WritingGrammarIssueAnalytics toAnalyticsDomain(WritingGrammarIssueAnalyticsEntity entity) {
        return new WritingGrammarIssueAnalytics(
                new WritingGrammarIssueAnalytics.WritingGrammarIssueAnalyticsId(entity.getId()),
                new WritingPracticeSession.WritingPracticeSessionId(entity.getSessionId()),
                new WritingPracticeScenario.WritingPracticeScenarioId(entity.getScenarioId()),
                new UserId(entity.getUserId()),
                entity.getGrammarRuleIdentifier(),
                entity.getIssueType(),
                entity.getPriority(),
                entity.getLearnerText(),
                entity.getCorrectedText(),
                entity.getShortExplanation(),
                entity.getOccurrenceCount(),
                entity.getCreatedAt()
        );
    }

    private String toJson(WritingStructuredFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(feedback);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to serialize writing structured feedback", exception);
        }
    }

    private WritingStructuredFeedback fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, WritingStructuredFeedback.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to deserialize writing structured feedback", exception);
        }
    }
}
