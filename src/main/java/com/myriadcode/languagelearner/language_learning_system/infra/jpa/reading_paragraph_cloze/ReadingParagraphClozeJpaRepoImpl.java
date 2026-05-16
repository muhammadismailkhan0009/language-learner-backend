package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeCard;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.mappers.ReadingParagraphClozeJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos.ReadingParagraphClozeSessionJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;

@Repository
public class ReadingParagraphClozeJpaRepoImpl implements ReadingParagraphClozeRepo {

    private static final ReadingParagraphClozeJpaMapper MAPPER = ReadingParagraphClozeJpaMapper.INSTANCE;

    private final ReadingParagraphClozeSessionJpaRepo sessionJpaRepo;
    private final EntityManager entityManager;

    public ReadingParagraphClozeJpaRepoImpl(ReadingParagraphClozeSessionJpaRepo sessionJpaRepo, EntityManager entityManager) {
        this.sessionJpaRepo = sessionJpaRepo;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ReadingParagraphClozeSession save(ReadingParagraphClozeSession session) {
        var entity = MAPPER.toEntity(session);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        if (sessionJpaRepo.existsById(entity.getId())) {
            entity.markExisting();
        }

        entity.setParagraphs(new LinkedHashSet<>(session.paragraphs().stream()
                .map(MAPPER::toParagraphEntity)
                .peek(paragraph -> {
                    if (paragraph.getCreatedAt() == null) {
                        paragraph.setCreatedAt(Instant.now());
                    }
                })
                .toList()));
        var paragraphsById = new HashMap<String, com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.ReadingParagraphClozeParagraphEntity>();
        entity.getParagraphs().forEach(paragraph -> paragraphsById.put(paragraph.getId(), paragraph));

        entity.setCards(new LinkedHashSet<>(session.cards().stream()
                .map(domainCard -> {
                    var card = MAPPER.toCardEntity(domainCard);
                    if (card.getCreatedAt() == null) {
                        card.setCreatedAt(Instant.now());
                    }
                    if (domainCard.paragraphId() != null) {
                        card.setParagraph(paragraphsById.get(domainCard.paragraphId()));
                    }
                    return card;
                })
                .toList()));

        var saved = sessionJpaRepo.saveAndFlush(entity);
        sessionJpaRepo.deleteOrphanParagraphs(saved.getId());
        sessionJpaRepo.flush();
        entityManager.clear();
        var reloaded = sessionJpaRepo.findById(saved.getId()).orElse(saved);
        return toDomain(reloaded);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReadingParagraphClozeSession> findLatestByUserId(String userId) {
        return sessionJpaRepo.findFirstByUserIdOrderByCreatedAtDesc(userId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReadingParagraphClozeSession> findByIdAndUserId(String sessionId, String userId) {
        return sessionJpaRepo.findByIdAndUserId(sessionId, userId).map(this::toDomain);
    }

    private ReadingParagraphClozeSession toDomain(com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.ReadingParagraphClozeSessionEntity entity) {
        var base = MAPPER.toDomain(entity);
        var allCards = entity.getCards().stream()
                .map(cardEntity -> {
                    var paragraphId = cardEntity.getParagraph() != null
                            ? cardEntity.getParagraph().getId()
                            : cardEntity.getParagraphId();
                    return new ReadingParagraphClozeCard(
                            new ReadingParagraphClozeCard.ReadingParagraphClozeCardId(cardEntity.getId()),
                            paragraphId,
                            cardEntity.getFlashcardId(),
                            cardEntity.getVocabularyId(),
                            cardEntity.getCreatedAt()
                    );
                })
                .toList();
        var paragraphs = entity.getParagraphs().stream()
                .map(paragraphEntity -> {
                    var paragraphBase = MAPPER.toParagraphDomain(paragraphEntity);
                    var cards = allCards.stream()
                            .filter(card -> paragraphBase.id().id().equals(card.paragraphId()))
                            .toList();
                    return new com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeParagraph(
                            paragraphBase.id(),
                            paragraphBase.paragraphIndex(),
                            paragraphBase.scenarioLabel(),
                            paragraphBase.clozeParagraph(),
                            paragraphBase.createdAt(),
                            cards
                    );
                })
                .toList();
        return new ReadingParagraphClozeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.clozeParagraph(),
                base.createdAt(),
                paragraphs,
                allCards
        );
    }
}
