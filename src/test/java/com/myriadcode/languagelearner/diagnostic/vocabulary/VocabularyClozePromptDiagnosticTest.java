package com.myriadcode.languagelearner.diagnostic.vocabulary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.ports.VocabularyClozeBatch;

import org.junit.jupiter.api.Disabled;
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
// @EnabledIfEnvironmentVariable(named = "RUN_CLOZE_PROMPT_DIAGNOSTIC", matches = "true")
class VocabularyClozePromptDiagnosticTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private LLMPort llmPort;

    @Test
    @DisplayName("manual diagnostic: prints the full cloze DTO returned by the LLM")
    void printFullClozeDtoForPromptTuning() throws JsonProcessingException {
        var topic = "Travel plans | Office small talk | Daily routines";
        var seeds = List.of(
                new VocabularyClozeGenerationSeed("seed-1", "gehen", "to go"),
                new VocabularyClozeGenerationSeed("seed-2", "abfahren", "to depart"),
                new VocabularyClozeGenerationSeed("seed-3", "der Termin", "appointment"),
                new VocabularyClozeGenerationSeed("seed-4", "pünktlich", "on time"),
                new VocabularyClozeGenerationSeed("seed-5", "verschieben", "to postpone")
        );

        VocabularyClozeBatch result = llmPort.generateVocabularyClozeSentences(topic, seeds);

        assertThat(result).isNotNull();

        System.out.println("=== Vocabulary Cloze Prompt Diagnostic DTO ===");
        System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }
}
