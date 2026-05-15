package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.mappers.ReadingParagraphClozeJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos.ReadingParagraphClozeSessionJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;

@Repository
public class ReadingParagraphClozeJpaRepoImpl implements ReadingParagraphClozeRepo {

    private static final ReadingParagraphClozeJpaMapper MAPPER = ReadingParagraphClozeJpaMapper.INSTANCE;

    private final ReadingParagraphClozeSessionJpaRepo sessionJpaRepo;

    public ReadingParagraphClozeJpaRepoImpl(ReadingParagraphClozeSessionJpaRepo sessionJpaRepo) {
        this.sessionJpaRepo = sessionJpaRepo;
    }

    @Override
    @Transactional
    public ReadingParagraphClozeSession save(ReadingParagraphClozeSession session) {
        var entity = MAPPER.toEntity(session);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        entity.setCards(new LinkedHashSet<>(session.cards().stream()
                .map(MAPPER::toCardEntity)
                .peek(card -> {
                    if (card.getCreatedAt() == null) {
                        card.setCreatedAt(Instant.now());
                    }
                })
                .toList()));

        return toDomain(sessionJpaRepo.save(entity));
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
        var cards = entity.getCards().stream().map(MAPPER::toCardDomain).toList();
        return new ReadingParagraphClozeSession(
                base.id(),
                base.userId(),
                base.topic(),
                base.clozeParagraph(),
                base.createdAt(),
                cards
        );
    }
}
