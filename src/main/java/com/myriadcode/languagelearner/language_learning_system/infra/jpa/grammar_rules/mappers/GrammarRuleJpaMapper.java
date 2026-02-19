package com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.mappers;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarExplanationParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenarioSentence;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities.GrammarRuleEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities.GrammarRuleExplanationParagraphEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities.GrammarScenarioEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.entities.GrammarScenarioSentenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GrammarRuleJpaMapper {

    GrammarRuleJpaMapper INSTANCE = Mappers.getMapper(GrammarRuleJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    GrammarRuleEntity toEntity(GrammarRule grammarRule);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "grammarRule", ignore = true)
    @Mapping(target = "paragraphText", source = "text")
    GrammarRuleExplanationParagraphEntity toExplanationParagraphEntity(GrammarExplanationParagraph paragraph);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "fixed", source = "isFixed")
    GrammarScenarioEntity toScenarioEntity(GrammarScenario scenario);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "grammarScenario", ignore = true)
    GrammarScenarioSentenceEntity toScenarioSentenceEntity(GrammarScenarioSentence sentence);

    @Mapping(target = "id.id", source = "id")
    GrammarRule toDomain(GrammarRuleEntity entity);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "text", source = "paragraphText")
    GrammarExplanationParagraph toExplanationParagraphDomain(GrammarRuleExplanationParagraphEntity paragraphEntity);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "isFixed", source = "fixed")
    GrammarScenario toScenarioDomain(GrammarScenarioEntity entity);

    @Mapping(target = "id.id", source = "id")
    GrammarScenarioSentence toScenarioSentenceDomain(GrammarScenarioSentenceEntity sentenceEntity);
}
