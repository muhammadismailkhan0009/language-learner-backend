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
    @Mapping(target = "scenario", source = "grammarScenario")
    GrammarRuleResponse toResponse(GrammarRule grammarRule);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "isFixed", source = "isFixed")
    GrammarRuleResponse.GrammarScenarioResponse toScenarioResponse(
            com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenario scenario
    );

    default String toExplanationParagraphText(GrammarExplanationParagraph paragraph) {
        return paragraph == null ? null : paragraph.text();
    }
}
