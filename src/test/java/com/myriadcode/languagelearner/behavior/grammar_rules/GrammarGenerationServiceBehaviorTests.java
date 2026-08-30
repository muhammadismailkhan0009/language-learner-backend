package com.myriadcode.languagelearner.behavior.grammar_rules;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.*;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarGenerationService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.*;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.*;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class GrammarGenerationServiceBehaviorTests {

    @Test
    void mcpDraftSubmissionStoresDraftsAndCompletesPendingWork() {
        var fixture = new Fixture();
        var request = fixture.addRequest(GrammarGenerationRequest.Type.RULE_DRAFT, List.of());
        fixture.jobs.createOrReplace("user-1", ContentGenerationJobType.GRAMMAR_RULE_DRAFT);

        var prompt = fixture.service.prepareRuleDraftPrompt("user-1");
        int stored = fixture.service.storeRuleDrafts("user-1",
                new GrammarGenerationService.GrammarRuleDraftSubmission(request.id().id(), List.of(
                        new GrammarRuleDraftProposal("word-order", "Word order", "B1", "de"))));

        assertThat(prompt.prompt()).isEqualTo("draft-prompt");
        assertThat(stored).isEqualTo(1);
        assertThat(fixture.rules.findByStatus("DRAFT")).hasSize(1);
        assertThat(fixture.jobRepo.findByUserIdAndType("user-1", ContentGenerationJobType.GRAMMAR_RULE_DRAFT)).isEmpty();
    }

    @Test
    void mcpDetailsSubmissionUpdatesRequestedDraftAndCompletesPendingWork() {
        var fixture = new Fixture();
        fixture.rules.save(draft("draft-1"));
        var request = fixture.addRequest(GrammarGenerationRequest.Type.RULE_DETAILS,
                List.of(new GrammarGenerationRequest.RuleSeed("draft-1", "word-order", "Word order")));
        fixture.jobs.createOrReplace("user-1", ContentGenerationJobType.GRAMMAR_RULE_DETAILS);

        fixture.service.storeRuleDetails("user-1", new GrammarGenerationService.GrammarRuleDetailsSubmission(
                request.id().id(), List.of(new GrammarGenerationService.GeneratedRuleDetails("draft-1",
                new GrammarRuleDraftDetails("word-order", "Word order", "B1", "de",
                        List.of("Explanation"), List.of(new GrammarRuleDraftDetails.GrammarRuleExample(
                        "Ich lerne.", "I learn.", null)))))));

        var stored = fixture.rules.findById("draft-1").orElseThrow();
        assertThat(stored.status()).isEqualTo("READY");
        assertThat(stored.explanationParagraphs()).extracting(GrammarExplanationParagraph::text)
                .containsExactly("Explanation");
    }

    private static GrammarRule draft(String id) {
        return new GrammarRule(new GrammarRule.GrammarRuleId(id), "word-order", "Word order", "B1", "DRAFT", false,
                List.of(), new GrammarScenario(new GrammarScenario.GrammarScenarioId("scenario-1"),
                "Examples", "Examples", "de", "LLM", false, List.of()));
    }

    private static final class Fixture {
        final RequestRepo requests = new RequestRepo();
        final RuleRepo rules = new RuleRepo();
        final JobRepo jobRepo = new JobRepo();
        final ContentGenerationJobService jobs = new ContentGenerationJobService(jobRepo);
        final GrammarGenerationService service = new GrammarGenerationService(requests, rules, jobs,
                new GrammarGenerationPromptApi() {
                    public String ruleDraftPrompt(String level, String language, int count, List<GrammarRuleCatalogContext> existing) { return "draft-prompt"; }
                    public String ruleDetailsPrompt(String level, String language, List<GrammarGenerationRequest.RuleSeed> seeds) { return "details-prompt"; }
                    public String levelReassignmentPrompt(List<GrammarLevelReassignmentInput> grammarRules) { return "level-prompt"; }
                });

        GrammarGenerationRequest addRequest(GrammarGenerationRequest.Type type,
                                            List<GrammarGenerationRequest.RuleSeed> seeds) {
            return requests.save(new GrammarGenerationRequest(
                    new GrammarGenerationRequest.GrammarGenerationRequestId(UUID.randomUUID().toString()),
                    new UserId("user-1"), type, "B1", "de", seeds, Instant.now()));
        }
    }

    private static final class RequestRepo implements GrammarGenerationRequestRepo {
        final List<GrammarGenerationRequest> values = new ArrayList<>();
        public GrammarGenerationRequest save(GrammarGenerationRequest value) { values.add(value); return value; }
        public Optional<GrammarGenerationRequest> findOldestByUserIdAndType(String userId, GrammarGenerationRequest.Type type) { return values.stream().filter(v -> v.userId().id().equals(userId) && v.type() == type).findFirst(); }
        public boolean existsByUserIdAndType(String userId, GrammarGenerationRequest.Type type) { return findOldestByUserIdAndType(userId, type).isPresent(); }
        public void delete(GrammarGenerationRequest.GrammarGenerationRequestId id) { values.removeIf(v -> v.id().equals(id)); }
    }

    private static final class RuleRepo implements GrammarRuleRepo {
        final Map<String, GrammarRule> values = new LinkedHashMap<>();
        public GrammarRule save(GrammarRule rule) { values.put(rule.id().id(), rule); return rule; }
        public Optional<GrammarRule> findById(String id) { return Optional.ofNullable(values.get(id)); }
        public List<GrammarRule> findAll() { return List.copyOf(values.values()); }
        public List<GrammarRule> findByStatus(String status) { return values.values().stream().filter(v -> status.equals(v.status())).toList(); }
        public void deleteById(String id) { values.remove(id); }
    }

    private static final class JobRepo implements ContentGenerationJobRepo {
        final Map<String, ContentGenerationJob> values = new HashMap<>();
        public ContentGenerationJob save(ContentGenerationJob job) { values.put(key(job.userId(), job.type()), job); return job; }
        public Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type) { return Optional.ofNullable(values.get(key(userId, type))); }
        public void deleteByUserIdAndType(String userId, ContentGenerationJobType type) { values.remove(key(userId, type)); }
        private String key(String userId, ContentGenerationJobType type) { return userId + ":" + type; }
    }
}
