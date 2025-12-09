package com.myriadcode.languagelearner.language_content.domain.repo;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.ContentGenerationQuantity;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class LanguageContentRepoTest {

    @Autowired
    private LanguageContentRepo languageContentRepo;

    @Test
    void getBlitzLessonsForWhichSentencesAreGenerated() {

//        0. define two language configs
        var lesson1 = new LangConfigsAdaptive(GermanAdaptive.GrammarRuleEnum.BASIC_PREPOSITIONS,
                GermanAdaptive.CommunicativeFunctionEnum.ASK_AND_ANSWER_SIMPLE_QUESTIONS,
                GermanAdaptive.ScenarioEnum.DIRECTIONS_AND_LOCATIONS,
                new LangConfigsAdaptive.GenerationQuantity(ContentGenerationQuantity.SENTENCES.getNumber()));

        var lesson2 = new LangConfigsAdaptive(GermanAdaptive.GrammarRuleEnum.ADJECTIVE_DECLENSION,
                GermanAdaptive.CommunicativeFunctionEnum.ASK_AND_ANSWER_SIMPLE_QUESTIONS,
                GermanAdaptive.ScenarioEnum.DIRECTIONS_AND_LOCATIONS,
                new LangConfigsAdaptive.GenerationQuantity(ContentGenerationQuantity.SENTENCES.getNumber()));
//        1 generate sentences(2 from same lesson, 1 from different)
        // 1️⃣ generate sentences (2 from same lesson, 1 from different)
        var sentences = List.of(
                new Sentence(
                        new Sentence.SentenceId("s1"),
                        new Sentence.SentenceData("A", "A"),
                        lesson1
                ),
                new Sentence(
                        new Sentence.SentenceId("s2"),
                        new Sentence.SentenceData("B", "B"),
                        lesson1
                ),
                new Sentence(
                        new Sentence.SentenceId("s3"),
                        new Sentence.SentenceData("C", "C"),
                        lesson2
                )
        );
        languageContentRepo.saveSentences(sentences);
        // 2️⃣ get blitz lessons
        var lessons =
                languageContentRepo.getBlitzLessonsForWhichSentencesAreGenerated();

        // 3️⃣ ensure that list size we got is 2
        assertEquals(2, lessons.size());
        assertTrue(lessons.contains(lesson1));
        assertTrue(lessons.contains(lesson2));

    }
}