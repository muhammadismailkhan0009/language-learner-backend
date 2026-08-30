package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarGenerationRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities.GrammarGenerationRequestEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.repos.GrammarGenerationRequestJpaRepo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class GrammarGenerationRequestJpaAdapter implements GrammarGenerationRequestRepo {
    private final GrammarGenerationRequestJpaRepo repo;

    GrammarGenerationRequestJpaAdapter(GrammarGenerationRequestJpaRepo repo) { this.repo = repo; }

    public GrammarGenerationRequest save(GrammarGenerationRequest request) {
        var rules = request.rules().stream().map(value -> new GrammarGenerationRequestEntity.RuleSeedData(
                value.draftId(), value.identifier(), value.name())).toList();
        return toDomain(repo.save(new GrammarGenerationRequestEntity(request.id().id(), request.userId().id(),
                request.type(), request.level(), request.targetLanguage(), rules, request.createdAt())));
    }

    public Optional<GrammarGenerationRequest> findOldestByUserIdAndType(String userId,
                                                                        GrammarGenerationRequest.Type type) {
        return repo.findFirstByUserIdAndTypeOrderByCreatedAtAsc(userId, type).map(this::toDomain);
    }

    public boolean existsByUserIdAndType(String userId, GrammarGenerationRequest.Type type) {
        return repo.existsByUserIdAndType(userId, type);
    }

    public void delete(GrammarGenerationRequest.GrammarGenerationRequestId id) { repo.deleteById(id.id()); }

    private GrammarGenerationRequest toDomain(GrammarGenerationRequestEntity entity) {
        return new GrammarGenerationRequest(new GrammarGenerationRequest.GrammarGenerationRequestId(entity.getId()),
                new UserId(entity.getUserId()), entity.getType(), entity.getLevel(), entity.getTargetLanguage(),
                entity.getRules().stream().map(value -> new GrammarGenerationRequest.RuleSeed(
                        value.draftId(), value.identifier(), value.name())).toList(), entity.getCreatedAt());
    }
}
