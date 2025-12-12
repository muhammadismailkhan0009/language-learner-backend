package com.myriadcode.languagelearner.language_content.infra.jpa.repos;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.infra.jpa.entities.SentenceEntity;
import com.myriadcode.languagelearner.language_content.infra.jpa.projections.ComboProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceEntityJpaRepo extends JpaRepository<SentenceEntity, String> {

    @Query("""
                SELECT DISTINCT s.scenario AS scenario,
                                s.grammarRule AS grammarRule,
                                s.communicationFunction AS communicationFunction
                FROM SentenceEntity s
            """)
    List<ComboProjection> findDistinctCombos();

    List<SentenceEntity> findAllByScenarioAndGrammarRuleAndCommunicationFunction(
            GermanAdaptive.ScenarioEnum scenario,
            GermanAdaptive.GrammarRuleEnum grammarRule,
            GermanAdaptive.CommunicativeFunctionEnum communicationFunction
    );

    List<SentenceEntity> findAllByScenario(GermanAdaptive.ScenarioEnum scenario);
}
