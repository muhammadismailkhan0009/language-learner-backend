package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarGenerationRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.repos.GrammarGenerationRequestJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class GrammarGenerationRequestJpaAdapterTests {
    @Autowired private GrammarGenerationRequestRepo adapter;
    @Autowired private GrammarGenerationRequestJpaRepo jpaRepo;

    @AfterEach
    void tearDown() { jpaRepo.deleteAll(); }

    @Test
    void roundTripsTypedGrammarRequestThroughPostgres() {
        var source = new GrammarGenerationRequest(new GrammarGenerationRequest.GrammarGenerationRequestId("r1"),
                new UserId("u1"), GrammarGenerationRequest.Type.RULE_DETAILS, "B1", "de",
                List.of(new GrammarGenerationRequest.RuleSeed("d1", "order", "Word order")),
                Instant.now().truncatedTo(ChronoUnit.MICROS));

        adapter.save(source);
        var loaded = adapter.findOldestByUserIdAndType("u1", GrammarGenerationRequest.Type.RULE_DETAILS).orElseThrow();

        assertThat(loaded).isEqualTo(source);
        assertThat(adapter.existsByUserIdAndType("u1", GrammarGenerationRequest.Type.RULE_DETAILS)).isTrue();
    }
}
