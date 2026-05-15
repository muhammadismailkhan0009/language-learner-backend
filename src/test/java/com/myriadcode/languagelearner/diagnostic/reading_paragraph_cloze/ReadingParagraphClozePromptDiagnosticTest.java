package com.myriadcode.languagelearner.diagnostic.reading_paragraph_cloze;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
// @EnabledIfEnvironmentVariable(named = "RUN_READING_CLOZE_PROMPT_DIAGNOSTIC", matches = "true")
class ReadingParagraphClozePromptDiagnosticTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private LLMPort llmPort;

    @Test
    @DisplayName("manual diagnostic: prints reading paragraph cloze DTO returned by LLM")
    void printReadingParagraphClozeDtoForPromptTuning() throws JsonProcessingException {
        var seeds = List.of(
                new ReadingPracticeVocabularySeed("abfahren", "to depart"),
                new ReadingPracticeVocabularySeed("verschieben", "to postpone"),
                new ReadingPracticeVocabularySeed("pünktlich", "on time"),
                new ReadingPracticeVocabularySeed("der Termin", "appointment"),
                new ReadingPracticeVocabularySeed("besprechen", "to discuss"),
                new ReadingPracticeVocabularySeed("die Verbindung", "connection"),
                new ReadingPracticeVocabularySeed("umsteigen", "to transfer"),
                new ReadingPracticeVocabularySeed("ankommen", "to arrive")
        );

        ReadingParagraphClozeGeneration result = llmPort.generateReadingParagraphCloze(
                "General practice",
                seeds,
                "B1"
        );

        assertThat(result).isNotNull();
        assertThat(result.paragraphs()).isNotNull().isNotEmpty();
        assertThat(result.paragraphs())
                .allSatisfy(paragraph -> {
                    assertThat(paragraph.scenarioLabel()).isNotBlank();
                    assertThat(paragraph.clozeParagraph()).isNotBlank();
                    assertThat(paragraph.items()).isNotNull().isNotEmpty();
                });

        System.out.println("=== Reading Paragraph Cloze Prompt Diagnostic DTO ===");
        System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }
}

