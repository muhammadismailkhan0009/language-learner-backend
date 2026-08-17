package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingGrammarEligibilityPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingGrammarEligibilityBehaviorTests {

    private final ReadingGrammarEligibilityPolicy policy = new ReadingGrammarEligibilityPolicy();

    @Test
    @DisplayName("A2 reading receives unique active A1 and A2 grammar titles in catalog order")
    void selectsEligibleGrammarTitles() {
        var candidates = List.of(
                new ReadingGrammarEligibilityPolicy.Candidate("Present tense", LanguageLevel.A1, true),
                new ReadingGrammarEligibilityPolicy.Candidate("Modal verbs", LanguageLevel.A2, true),
                new ReadingGrammarEligibilityPolicy.Candidate("Relative clauses", LanguageLevel.B1, true),
                new ReadingGrammarEligibilityPolicy.Candidate("Inactive basics", LanguageLevel.A1, false),
                new ReadingGrammarEligibilityPolicy.Candidate("  Present tense  ", LanguageLevel.A1, true),
                new ReadingGrammarEligibilityPolicy.Candidate(" ", LanguageLevel.A1, true)
        );

        var titles = policy.selectTitles(LanguageLevel.A2, candidates);

        assertThat(titles).containsExactly("Present tense", "Modal verbs");
    }

    @Test
    @DisplayName("Reading grammar selection tolerates missing or invalid candidate levels")
    void ignoresCandidatesWithoutValidLevels() {
        var titles = policy.selectTitles(LanguageLevel.A1, List.of(
                new ReadingGrammarEligibilityPolicy.Candidate("Unknown", null, true),
                new ReadingGrammarEligibilityPolicy.Candidate("Present tense", LanguageLevel.A1, true)
        ));

        assertThat(titles).containsExactly("Present tense");
    }
}
