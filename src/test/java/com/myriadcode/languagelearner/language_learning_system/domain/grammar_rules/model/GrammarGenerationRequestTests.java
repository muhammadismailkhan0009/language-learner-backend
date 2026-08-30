package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model;

import com.myriadcode.languagelearner.common.ids.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrammarGenerationRequestTests {

    @Test
    void retainsTypedGrammarInputOutsideGenericJob() {
        var request = new GrammarGenerationRequest(
                new GrammarGenerationRequest.GrammarGenerationRequestId("request-1"),
                new UserId("user-1"),
                GrammarGenerationRequest.Type.RULE_DETAILS,
                "B1",
                "de",
                List.of(new GrammarGenerationRequest.RuleSeed("draft-1", "word-order", "Word order")),
                Instant.parse("2026-08-30T00:00:00Z")
        );

        assertThat(request.userId().id()).isEqualTo("user-1");
        assertThat(request.type()).isEqualTo(GrammarGenerationRequest.Type.RULE_DETAILS);
        assertThat(request.rules()).containsExactly(
                new GrammarGenerationRequest.RuleSeed("draft-1", "word-order", "Word order")
        );
    }
}
