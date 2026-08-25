package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeReadingContent;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.services.reading_practice.ReadingPracticeReadingContentValidator;
import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ReadingMultiScenarioContentContractTests {
    @Test
    void existingReadingPromptCarriesMultiScenarioInputs() {
        var prompt = PromptsGenerator.readingContentParagraphs(
                List.of(new ReadingPracticeVocabularySeed("v1", "gehen", "go")),
                List.of("Old scenario"), LanguageLevel.A2, List.of("Modal verbs"), 3);
        assertThat(prompt).contains("Required Scenarios: 3", "id=v1", "Old scenario", "Modal verbs")
                .contains("Generate exactly the requested number of scenarios", "PARAGRAPHS");
    }

    @Test
    void existingReadingContentShapeValidatesScenarioHierarchy() {
        var content = new ReadingPracticeReadingContent(List.of(
                scenario("One"), scenario("Two"), scenario("Three")));
        assertThat(new ReadingPracticeReadingContentValidator().validate(3,
                List.of(new ReadingPracticeVocabularySeed("v1", "gehen", "go")), content)).isEmpty();
    }

    @Test
    void unknownIdFallsBackToNormalizedSurface() {
        var scenario = new ReadingPracticeReadingContent.Scenario("One",
                List.of(new ReadingPracticeReadingContent.Paragraph("Ich gehe.", List.of("Ich gehe."))),
                List.of(new ReadingPracticeReadingContent.UsedVocabulary("wrong-id", " GEHEN ")));
        var content = new ReadingPracticeReadingContent(List.of(scenario, scenario("Two"), scenario("Three")));

        assertThat(new ReadingPracticeReadingContentValidator().validate(3,
                List.of(new ReadingPracticeVocabularySeed("v1", "gehen", "go")), content)).isEmpty();
    }

    private ReadingPracticeReadingContent.Scenario scenario(String label) {
        return new ReadingPracticeReadingContent.Scenario(label,
                List.of(new ReadingPracticeReadingContent.Paragraph("Ich gehe.", List.of("Ich gehe."))),
                List.of(new ReadingPracticeReadingContent.UsedVocabulary("v1", "gehen")));
    }
}
