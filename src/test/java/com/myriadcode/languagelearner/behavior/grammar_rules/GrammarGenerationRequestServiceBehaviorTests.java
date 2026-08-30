package com.myriadcode.languagelearner.behavior.grammar_rules;

import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarGenerationRequestService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarGenerationRequestRepo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GrammarGenerationRequestServiceBehaviorTests {
    private final RequestRepo requests = new RequestRepo();
    private final JobRepo jobs = new JobRepo();
    private final GrammarGenerationRequestService service = new GrammarGenerationRequestService(
            requests, new ContentGenerationJobService(jobs), null);

    @Test
    void draftRequestStoresTypedInputAndCreatesMarkerJob() {
        service.requestRuleDrafts("user-1", "b1");

        var request = requests.values.getFirst();
        assertThat(request.userId().id()).isEqualTo("user-1");
        assertThat(request.type()).isEqualTo(GrammarGenerationRequest.Type.RULE_DRAFT);
        assertThat(request.level()).isEqualTo("B1");
        assertThat(jobs.findByUserIdAndType("user-1", ContentGenerationJobType.GRAMMAR_RULE_DRAFT)).isPresent();
    }

    @Test
    void detailsRequestStoresSelectedRulesAndCreatesMarkerJob() {
        service.requestRuleDetails("user-1", "b2", "de", List.of(
                new GrammarGenerationRequest.RuleSeed("draft-1", "word-order", "Word order")
        ));

        var request = requests.values.getFirst();
        assertThat(request.type()).isEqualTo(GrammarGenerationRequest.Type.RULE_DETAILS);
        assertThat(request.rules()).extracting(GrammarGenerationRequest.RuleSeed::draftId)
                .containsExactly("draft-1");
        assertThat(jobs.findByUserIdAndType("user-1", ContentGenerationJobType.GRAMMAR_RULE_DETAILS)).isPresent();
    }

    @Test
    void reassignmentRequestNeedsOnlyMarkerBecauseRulesAreStoredAlready() {
        service.requestLevelReassignment("user-1");

        assertThat(requests.values).isEmpty();
        assertThat(jobs.findByUserIdAndType("user-1", ContentGenerationJobType.GRAMMAR_LEVEL_REASSIGNMENT)).isPresent();
    }

    private static final class RequestRepo implements GrammarGenerationRequestRepo {
        private final List<GrammarGenerationRequest> values = new ArrayList<>();
        public GrammarGenerationRequest save(GrammarGenerationRequest request) { values.add(request); return request; }
        public Optional<GrammarGenerationRequest> findOldestByUserIdAndType(String userId, GrammarGenerationRequest.Type type) {
            return values.stream().filter(value -> value.userId().id().equals(userId) && value.type() == type).findFirst();
        }
        public boolean existsByUserIdAndType(String userId, GrammarGenerationRequest.Type type) {
            return findOldestByUserIdAndType(userId, type).isPresent();
        }
        public void delete(GrammarGenerationRequest.GrammarGenerationRequestId id) {
            values.removeIf(value -> value.id().equals(id));
        }
    }

    private static final class JobRepo implements ContentGenerationJobRepo {
        private final Map<String, ContentGenerationJob> values = new HashMap<>();
        public ContentGenerationJob save(ContentGenerationJob job) { values.put(key(job.userId(), job.type()), job); return job; }
        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) {
            return Optional.ofNullable(values.get(key(userId, type)));
        }
        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) { values.remove(key(userId, type)); }
        private String key(String userId, ContentGenerationJobType type) { return userId + ":" + type; }
    }
}
