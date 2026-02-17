package com.myriadcode.languagelearner.language_learning_system.domain.listening;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.application.services.listening.ListeningStepOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.listening.model.WordToListenTo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.listening.repos.WordToListenToEntityJpaRepo;
import com.myriadcode.languagelearner.user_management.infra.jpa.entities.UserInfoEntity;
import com.myriadcode.languagelearner.user_management.infra.jpa.repos.UserInfoJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
public class ListenToWordFlowTests {

    @Autowired
    private ListeningStepOrchestrationService listeningStepOrchestrationService;

    @Autowired
    private WordToListenToEntityJpaRepo wordToListenToEntityJpaRepo;

    @Autowired
    private UserInfoJpaRepo userInfoJpaRepo;

    private String userId = "user_id";

    @BeforeEach
    public void setUp() {
        var user = new UserInfoEntity();
        user.setId(userId);
        user.setUsername("listener");
        user.setPassword("password");
        userInfoJpaRepo.save(user);
    }

    @AfterEach
    public void tearDown() {
        wordToListenToEntityJpaRepo.deleteAll();
        userInfoJpaRepo.deleteAll();
    }

    @Test
    public void storeWord() {
        var saved = listeningStepOrchestrationService.saveWordToListenTo(
                userId, new WordToListenTo(null, "hallo"));

        assertThat(saved.word()).isEqualTo("hallo");
        assertThat(wordToListenToEntityJpaRepo.count()).isEqualTo(1);
    }

    @Test
    public void retrieveWord() {
        listeningStepOrchestrationService.saveWordToListenTo(userId, new WordToListenTo(null, "eins"));
        listeningStepOrchestrationService.saveWordToListenTo(userId, new WordToListenTo(null, "zwei"));

        var words = listeningStepOrchestrationService.fetchWordsToListenTo(userId);

        assertThat(words).extracting(WordToListenTo::word)
                .containsExactlyInAnyOrder("eins", "zwei");
    }
}
