package com.myriadcode.languagelearner.language_learning_system.application.mappers.grammar_rules;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.CreateGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.EditGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleResponse;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarExplanationParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services.GrammarRuleDomainService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GrammarRuleApiMapper {

    GrammarRuleApiMapper INSTANCE = Mappers.getMapper(GrammarRuleApiMapper.class);

    GrammarRuleDomainService.GrammarScenarioCreateInput toCreateScenarioInput(
            CreateGrammarRuleRequest.GrammarScenarioRequest scenario
    );

    GrammarRuleDomainService.GrammarScenarioPatchInput toPatchScenarioInput(
            EditGrammarRuleRequest.GrammarScenarioUpdateRequest scenario
    );

    GrammarRuleDomainService.GrammarScenarioSentenceInput toScenarioSentenceInput(
            CreateGrammarRuleRequest.GrammarScenarioSentenceRequest sentence
    );

    GrammarRuleDomainService.GrammarScenarioSentenceInput toScenarioSentenceInput(
            EditGrammarRuleRequest.GrammarScenarioSentenceUpdateRequest sentence
    );

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "explanationExamples", expression = "java(toExplanationExamples(grammarRule))")
    GrammarRuleResponse toResponse(GrammarRule grammarRule);

    default String toExplanationParagraphText(GrammarExplanationParagraph paragraph) {
        return paragraph == null ? null : paragraph.text();
    }

    default List<GrammarRuleResponse.GrammarExplanationExampleResponse> toExplanationExamples(GrammarRule grammarRule) {
        if (grammarRule == null || grammarRule.grammarScenario() == null || grammarRule.grammarScenario().sentences() == null) {
            return List.of();
        }
        return grammarRule.grammarScenario().sentences().stream()
                .map(sentence -> new GrammarRuleResponse.GrammarExplanationExampleResponse(
                        sentence.sentence(),
                        sentence.translation(),
                        null
                ))
                .toList();
    }
}
