package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarExplanationParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenarioSentence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GrammarRuleDomainService {

    private static final String SYSTEM_CREATOR = "SYSTEM";

    private GrammarRuleDomainService() {
    }

    public static GrammarRule create(String name, List<String> explanationParagraphs,
                                     GrammarScenarioCreateInput grammarScenarioInput) {
        validateName(name);
        var normalizedParagraphs = normalizeParagraphs(explanationParagraphs);
        var scenario = createGrammarScenario(grammarScenarioInput);

        return new GrammarRule(
                new GrammarRule.GrammarRuleId(UUID.randomUUID().toString()),
                name,
                normalizedParagraphs,
                scenario
        );
    }

    public static GrammarRule edit(GrammarRule existingGrammarRule, String name, List<String> explanationParagraphs,
                                   GrammarScenarioPatchInput grammarScenarioPatchInput) {
        var updatedName = name == null ? existingGrammarRule.name() : name;
        validateName(updatedName);

        var updatedParagraphs = explanationParagraphs == null
                ? existingGrammarRule.explanationParagraphs()
                : normalizeParagraphs(explanationParagraphs);

        var updatedScenario = grammarScenarioPatchInput == null
                ? existingGrammarRule.grammarScenario()
                : patchScenario(existingGrammarRule.grammarScenario(), grammarScenarioPatchInput);

        return new GrammarRule(
                existingGrammarRule.id(),
                updatedName,
                updatedParagraphs,
                updatedScenario
        );
    }

    private static GrammarScenario createGrammarScenario(GrammarScenarioCreateInput grammarScenarioInput) {
        if (grammarScenarioInput == null) {
            throw new IllegalArgumentException("Grammar scenario is required");
        }

        validateScenarioTitle(grammarScenarioInput.title());
        validateScenarioDescription(grammarScenarioInput.description());
        validateTargetLanguage(grammarScenarioInput.targetLanguage());
        var sentences = normalizeScenarioSentences(grammarScenarioInput.sentences());

        return new GrammarScenario(
                new GrammarScenario.GrammarScenarioId(UUID.randomUUID().toString()),
                grammarScenarioInput.title(),
                grammarScenarioInput.description(),
                grammarScenarioInput.targetLanguage(),
                SYSTEM_CREATOR,
                true,
                sentences
        );
    }

    private static GrammarScenario patchScenario(GrammarScenario existingScenario,
                                                 GrammarScenarioPatchInput grammarScenarioPatchInput) {
        var updatedTitle = grammarScenarioPatchInput.title() == null
                ? existingScenario.title()
                : grammarScenarioPatchInput.title();
        var updatedDescription = grammarScenarioPatchInput.description() == null
                ? existingScenario.description()
                : grammarScenarioPatchInput.description();
        var updatedTargetLanguage = grammarScenarioPatchInput.targetLanguage() == null
                ? existingScenario.targetLanguage()
                : grammarScenarioPatchInput.targetLanguage();
        var updatedSentences = grammarScenarioPatchInput.sentences() == null
                ? existingScenario.sentences()
                : normalizeScenarioSentences(grammarScenarioPatchInput.sentences());

        validateScenarioTitle(updatedTitle);
        validateScenarioDescription(updatedDescription);
        validateTargetLanguage(updatedTargetLanguage);

        return new GrammarScenario(
                existingScenario.id(),
                updatedTitle,
                updatedDescription,
                updatedTargetLanguage,
                existingScenario.createdBy(),
                true,
                updatedSentences
        );
    }

    private static List<GrammarExplanationParagraph> normalizeParagraphs(List<String> explanationParagraphs) {
        if (explanationParagraphs == null || explanationParagraphs.isEmpty()) {
            throw new IllegalArgumentException("Grammar explanation must contain at least one paragraph");
        }

        var normalized = new ArrayList<GrammarExplanationParagraph>();
        for (int i = 0; i < explanationParagraphs.size(); i++) {
            var paragraph = explanationParagraphs.get(i);
            if (paragraph == null || paragraph.isBlank()) {
                throw new IllegalArgumentException("Grammar explanation paragraph is required");
            }
            normalized.add(new GrammarExplanationParagraph(
                    new GrammarExplanationParagraph.GrammarExplanationParagraphId(UUID.randomUUID().toString()),
                    paragraph,
                    i
            ));
        }
        return normalized;
    }

    private static List<GrammarScenarioSentence> normalizeScenarioSentences(
            List<GrammarScenarioSentenceInput> sentenceInputs
    ) {
        if (sentenceInputs == null || sentenceInputs.isEmpty()) {
            throw new IllegalArgumentException("Grammar scenario must contain at least one sentence");
        }

        var normalized = new ArrayList<GrammarScenarioSentence>();
        for (int i = 0; i < sentenceInputs.size(); i++) {
            var sentenceInput = sentenceInputs.get(i);
            validateScenarioSentence(sentenceInput);
            normalized.add(new GrammarScenarioSentence(
                    new GrammarScenarioSentence.GrammarScenarioSentenceId(UUID.randomUUID().toString()),
                    sentenceInput.sentence(),
                    sentenceInput.translation(),
                    i
            ));
        }
        return normalized;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Grammar rule name is required");
        }
    }

    private static void validateScenarioTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Grammar scenario title is required");
        }
    }

    private static void validateScenarioDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Grammar scenario description is required");
        }
    }

    private static void validateTargetLanguage(String targetLanguage) {
        if (targetLanguage == null || targetLanguage.isBlank()) {
            throw new IllegalArgumentException("Target language is required");
        }
    }

    private static void validateScenarioSentence(GrammarScenarioSentenceInput sentenceInput) {
        if (sentenceInput == null || sentenceInput.sentence() == null || sentenceInput.sentence().isBlank()) {
            throw new IllegalArgumentException("Grammar scenario sentence is required");
        }
        if (sentenceInput.translation() == null || sentenceInput.translation().isBlank()) {
            throw new IllegalArgumentException("Grammar scenario sentence translation is required");
        }
    }

    public record GrammarScenarioCreateInput(String title, String description, String targetLanguage,
                                             List<GrammarScenarioSentenceInput> sentences) {
    }

    public record GrammarScenarioPatchInput(String title, String description, String targetLanguage,
                                            List<GrammarScenarioSentenceInput> sentences) {
    }

    public record GrammarScenarioSentenceInput(String sentence, String translation) {
    }
}
