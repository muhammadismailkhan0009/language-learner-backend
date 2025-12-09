package com.myriadcode.languagelearner.language_content.infra.jpa.mappers;

import com.myriadcode.languagelearner.language_content.domain.model.UserStatsForContent;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.infra.jpa.entities.UserStatsForContentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserStatsMapper {

    UserStatsMapper INSTANCE = Mappers.getMapper(UserStatsMapper.class);

    @Mappings({
            @Mapping(target = "id", source = "id.id"),
            @Mapping(target = "userId", source = "userId.id"),

            @Mapping(target = "scenario", source = "langConfigsAdaptive.scenario"),
            @Mapping(target = "function", source = "langConfigsAdaptive.function"),
            @Mapping(target = "grammarRule", source = "langConfigsAdaptive.rule")
    })
    UserStatsForContentEntity toEntity(UserStatsForContent userStatsForContent);

    @Mappings({
            @Mapping(source = "id", target = "id.id"),
            @Mapping(source = "userId", target = "userId.id"),

            @Mapping(target = "langConfigsAdaptive", source = ".", qualifiedByName = "mapConfigsForStats")
    })
    UserStatsForContent toDomain(UserStatsForContentEntity userStatsForContentEntity);

    @Named("mapConfigsForStats")
    default LangConfigsAdaptive mapConfigsForStats(UserStatsForContentEntity sentence) {
        return new LangConfigsAdaptive(

                sentence.getGrammarRule(),
                sentence.getFunction(),
                sentence.getScenario(),
                new LangConfigsAdaptive.GenerationQuantity(8)
        );
    }
}
