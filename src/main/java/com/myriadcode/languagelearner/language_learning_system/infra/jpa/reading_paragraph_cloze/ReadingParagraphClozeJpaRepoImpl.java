package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.*;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.*;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos.ReadingParagraphClozeSessionJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
public class ReadingParagraphClozeJpaRepoImpl implements ReadingParagraphClozeRepo {
    private final ReadingParagraphClozeSessionJpaRepo jpaRepo;

    public ReadingParagraphClozeJpaRepoImpl(ReadingParagraphClozeSessionJpaRepo jpaRepo) { this.jpaRepo = jpaRepo; }

    @Override @Transactional
    public ReadingParagraphClozeSession save(ReadingParagraphClozeSession session) {
        return toDomain(jpaRepo.saveAndFlush(toEntity(session)));
    }

    @Override @Transactional(readOnly = true)
    public List<ReadingParagraphClozeSession> findAllByUserId(String userId) {
        return jpaRepo.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDomain).toList();
    }

    @Override @Transactional(readOnly = true)
    public Optional<ReadingParagraphClozeSession> findByIdAndUserId(String sessionId, String userId) {
        return jpaRepo.findByIdAndUserId(sessionId, userId).map(this::toDomain);
    }

    @Override @Transactional
    public boolean deleteByIdAndUserId(String sessionId, String userId) {
        var entity = jpaRepo.findByIdAndUserId(sessionId, userId).orElse(null);
        if (entity == null) return false;
        jpaRepo.delete(entity);
        jpaRepo.flush();
        return true;
    }

    private ReadingParagraphClozeSessionEntity toEntity(ReadingParagraphClozeSession session) {
        var entity = new ReadingParagraphClozeSessionEntity();
        entity.setId(session.id().id()); entity.setUserId(session.userId().id());
        entity.setLearnerLevel(session.learnerLevel().name()); entity.setCreatedAt(session.createdAt());
        entity.setParagraphs(new LinkedHashSet<>(session.paragraphs().stream().map(this::toParagraphEntity).toList()));
        return entity;
    }

    private ReadingParagraphClozeParagraphEntity toParagraphEntity(ReadingParagraphClozeParagraph paragraph) {
        var entity = new ReadingParagraphClozeParagraphEntity();
        entity.setId(paragraph.id().id()); entity.setParagraphIndex(paragraph.paragraphIndex());
        entity.setScenarioLabel(paragraph.scenarioLabel()); entity.setClozeParagraph(paragraph.clozeParagraph());
        entity.setBlanks(new LinkedHashSet<>(paragraph.blanks().stream().map(this::toBlankEntity).toList()));
        return entity;
    }

    private ReadingParagraphClozeBlankEntity toBlankEntity(ReadingParagraphClozeBlank blank) {
        var entity = new ReadingParagraphClozeBlankEntity();
        entity.setId(blank.id().id()); entity.setBlankIndex(blank.blankIndex()); entity.setBlankToken(blank.blankToken());
        entity.setExactAnswer(blank.exactAnswer()); entity.setAnswerExplanation(blank.answerExplanation());
        entity.setPracticeKind(blank.practiceKind().name()); entity.setVocabularyId(blank.vocabularyId());
        entity.setGrammarRuleIds(new LinkedHashSet<>(blank.grammarRuleIds()));
        return entity;
    }

    private ReadingParagraphClozeSession toDomain(ReadingParagraphClozeSessionEntity entity) {
        return new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId(entity.getId()),
                new UserId(entity.getUserId()), LanguageLevel.from(entity.getLearnerLevel()), entity.getCreatedAt(),
                entity.getParagraphs().stream().map(this::toParagraphDomain).toList());
    }

    private ReadingParagraphClozeParagraph toParagraphDomain(ReadingParagraphClozeParagraphEntity entity) {
        return new ReadingParagraphClozeParagraph(
                new ReadingParagraphClozeParagraph.ReadingParagraphClozeParagraphId(entity.getId()),
                entity.getParagraphIndex(), entity.getScenarioLabel(), entity.getClozeParagraph(),
                entity.getBlanks().stream().map(this::toBlankDomain).toList());
    }

    private ReadingParagraphClozeBlank toBlankDomain(ReadingParagraphClozeBlankEntity entity) {
        return new ReadingParagraphClozeBlank(
                new ReadingParagraphClozeBlank.ReadingParagraphClozeBlankId(entity.getId()), entity.getBlankIndex(),
                entity.getBlankToken(), entity.getExactAnswer(), entity.getAnswerExplanation(),
                com.myriadcode.languagelearner.common.enums.ClozePracticeKind.valueOf(entity.getPracticeKind()), entity.getVocabularyId(),
                List.copyOf(entity.getGrammarRuleIds()));
    }
}
