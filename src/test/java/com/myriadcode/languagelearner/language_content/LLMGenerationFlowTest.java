package com.myriadcode.languagelearner.language_content;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
@Disabled("Enable it when changes in llm occur.")
public class LLMGenerationFlowTest {

    @Autowired
    private LLMPort llmport;

    LangConfigsAdaptive languageConfigs = new LangConfigsAdaptive(
            GermanAdaptive.GrammarRuleEnum.PRESENT_TENSE,
            GermanAdaptive.CommunicativeFunctionEnum.GREET,
            GermanAdaptive.ScenarioEnum.SELF_INTRODUCTION,
            new LangConfigsAdaptive.GenerationQuantity(8)
    );

    LangConfigsAdaptive languageConfigs2 = new LangConfigsAdaptive(
            GermanAdaptive.GrammarRuleEnum.MAIN_CLAUSE_WORD_ORDER,
            GermanAdaptive.CommunicativeFunctionEnum.INTRODUCE_SELF,
            GermanAdaptive.ScenarioEnum.SELF_INTRODUCTION,
            new LangConfigsAdaptive.GenerationQuantity(8)
    );

    LangConfigsAdaptive languageConfig3 = new LangConfigsAdaptive(
            GermanAdaptive.GrammarRuleEnum.WH_QUESTIONS,
            GermanAdaptive.CommunicativeFunctionEnum.ASK_AND_ANSWER_SIMPLE_QUESTIONS,
            GermanAdaptive.ScenarioEnum.SELF_INTRODUCTION,
            new LangConfigsAdaptive.GenerationQuantity(8)
    );

    @Test
    public void generateFullContent() {
        var sentences = llmport.generateSentences(languageConfigs,List.of());
        assertThat(sentences.size()).isEqualTo(languageConfigs.quantity().sentenceCount());
//        var chunks = llmport.generateChunks(languageConfigs, sentences, List.of());
//        assertThat(chunks.size()).isGreaterThan(0);
//        var vocab = llmport.generateVocabulary(languageConfigs, chunks, sentences);
//        assertThat(vocab.size()).isGreaterThan(0);

//        generate chunks

        var newSentences2 = llmport.generateSentences(languageConfigs2,sentences);
        assertThat(newSentences2.size()).isEqualTo(languageConfigs.quantity().sentenceCount());

        var previousCombined = Stream.concat(newSentences2.stream(), sentences.stream()).toList();
        var newSentences3 = llmport.generateSentences(languageConfig3,previousCombined);
        assertThat(newSentences3.size()).isEqualTo(languageConfigs.quantity().sentenceCount());
    }

}
