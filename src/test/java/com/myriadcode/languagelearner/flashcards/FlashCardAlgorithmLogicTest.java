package com.myriadcode.languagelearner.flashcards;

import com.myriadcode.languagelearner.common.enums.DeckInfo;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import com.myriadcode.languagelearner.language_content.application.publishers.ContentPublisher;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.domain.repo.LanguageContentRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.ChunkEntityJpaRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.SentenceEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
public class FlashCardAlgorithmLogicTest {

    @Autowired
    private LanguageContentRepo languageContentRepo;
    @Autowired
    private CardStudyService cardStudyService;

    @Autowired
    private ContentPublisher contentPublisher;

    //    to clean up only
    @Autowired
    private ChunkEntityJpaRepo chunkEntityJpaRepo;
    @Autowired
    private FlashCardReviewJpaRepo flashCardReviewJpaRepo;

    private String userId = "user_id";

    LangConfigsAdaptive languageConfigs = new LangConfigsAdaptive(

            GermanAdaptive.GrammarRuleEnum.BASIC_PREPOSITIONS,
            GermanAdaptive.CommunicativeFunctionEnum.ASK_AND_ANSWER_SIMPLE_QUESTIONS,
            GermanAdaptive.ScenarioEnum.DIRECTIONS_AND_LOCATIONS,
            new LangConfigsAdaptive.GenerationQuantity(8)
    );
    @Autowired
    private SentenceEntityJpaRepo sentenceEntityJpaRepo;

    @AfterEach
    public void tearDown() {
        chunkEntityJpaRepo.deleteAll();
        sentenceEntityJpaRepo.deleteAll();
        flashCardReviewJpaRepo.deleteAll();
        System.out.println("clean up completed");
    }

    @Test
    public void testNextCardToStudy() {

//        1- first create some cards for deck
        var sentences = List.of(
                new Sentence(new Sentence.SentenceId("id"),
                        new Sentence.SentenceData("sentence", "translation"),
                        languageConfigs),
                new Sentence(new Sentence.SentenceId("id2"),
                        new Sentence.SentenceData("sentence", "translation"),
                        languageConfigs));

        languageContentRepo.saveSentences(sentences);
        assertThat(sentenceEntityJpaRepo.count()).isEqualTo(sentences.size());
//        this send cards info to flashcards module and generate card there
        contentPublisher.createSentencesCards(sentences, userId,false);
        assertThat(flashCardReviewJpaRepo.count()).isEqualTo(sentences.size());
//        now, we see that our next card to study is not empty
        var cardToStudy = cardStudyService.getNextCardToStudy(DeckInfo.SENTENCES, userId);
        assertThat(cardToStudy).isPresent();
    }
}
