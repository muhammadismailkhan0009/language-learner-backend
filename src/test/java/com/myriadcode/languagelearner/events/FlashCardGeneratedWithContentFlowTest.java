package com.myriadcode.languagelearner.events;


import com.myriadcode.languagelearner.common.enums.DeckInfo;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ids.DeckId;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import com.myriadcode.languagelearner.language_content.application.publishers.ContentPublisher;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.ChunkEntityJpaRepo;
import com.myriadcode.languagelearner.language_content.infra.jpa.repos.SentenceEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
public class FlashCardGeneratedWithContentFlowTest {

    @Autowired
    private ContentPublisher contentPublisher;

    @Autowired
    private FlashCardRepo flashCardRepo;


    //    manual cleanup
    @Autowired
    private ChunkEntityJpaRepo chunkEntityJpaRepo;
    @Autowired
    private SentenceEntityJpaRepo sentenceEntityJpaRepo;
    @Autowired
    private FlashCardReviewJpaRepo flashCardReviewJpaRepo;

    private String userId = "user_id";

    @AfterEach
    public void tearDown() {
        chunkEntityJpaRepo.deleteAll();
        sentenceEntityJpaRepo.deleteAll();

        flashCardReviewJpaRepo.findAll().forEach(card -> System.out.println(card.getId()));
        flashCardReviewJpaRepo.deleteAll();
        System.out.println("clean up completed");
        flashCardReviewJpaRepo.findAll().forEach(System.out::println);


    }

    @BeforeEach
    public void setUp() {
        chunkEntityJpaRepo.deleteAll();
        sentenceEntityJpaRepo.deleteAll();
        flashCardReviewJpaRepo.deleteAll();
    }

    @Test
    public void checkCardsAreGeneratedForSentences() {

        var sentences = List.of(
                new Sentence(new Sentence.SentenceId(UUID.randomUUID().toString()),
                        new Sentence.SentenceData("a", "b"), null),
                new Sentence(new Sentence.SentenceId(UUID.randomUUID().toString()),
                        new Sentence.SentenceData("d", "e"), null));

//        this send cards info to flashcards module and generate card there
        contentPublisher.createSentencesCards(sentences, userId,false);

//        now, we test if cards are generated against given chunk id
        var cards = flashCardRepo.findFlashCardsByDeckAndUser(new DeckId(DeckInfo.SENTENCES.getId()), userId);
        assertThat(cards.size()).isEqualTo(sentences.size());
    }
}
