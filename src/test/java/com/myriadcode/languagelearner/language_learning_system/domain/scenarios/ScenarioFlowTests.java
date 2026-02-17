package com.myriadcode.languagelearner.language_learning_system.domain.scenarios;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.CreateScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request.EditScenarioRequest;
import com.myriadcode.languagelearner.language_learning_system.scenarios.application.services.ScenarioOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.scenarios.infra.jpa.repos.ScenarioEntityJpaRepo;
import com.myriadcode.languagelearner.user_management.infra.jpa.entities.UserInfoEntity;
import com.myriadcode.languagelearner.user_management.infra.jpa.repos.UserInfoJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
public class ScenarioFlowTests {

    @Autowired
    private ScenarioOrchestrationService scenarioOrchestrationService;

    @Autowired
    private ScenarioEntityJpaRepo scenarioEntityJpaRepo;

    @Autowired
    private UserInfoJpaRepo userInfoJpaRepo;

    private final String userAId = "scenario_user_a";
    private final String userBId = "scenario_user_b";

    @BeforeEach
    public void setUp() {
        createUser(userAId, "scenario_a");
        createUser(userBId, "scenario_b");
    }

    @AfterEach
    public void tearDown() {
        scenarioEntityJpaRepo.deleteAll();
        userInfoJpaRepo.deleteAll();
    }

    @Test
    public void storeScenario() {
        var saved = scenarioOrchestrationService.createScenario(
                userAId,
                new CreateScenarioRequest(
                        "TALKING_FOR_LEARNING",
                        "de",
                        List.of(
                                new CreateScenarioRequest.ScenarioSentenceRequest("Guten Tag", "Good day"),
                                new CreateScenarioRequest.ScenarioSentenceRequest("Wie geht's?", "How are you?")
                        )
                )
        );

        assertThat(saved.sentences()).hasSize(2);
        assertThat(saved.id()).isNotBlank();
        assertThat(saved.nature()).isEqualTo("TALKING_FOR_LEARNING");
        assertThat(scenarioEntityJpaRepo.count()).isEqualTo(1);
    }

    @Test
    public void editScenarioUpdatesExistingSentenceAndAddsNewSentenceAndRemovesOmitted() {
        var saved = scenarioOrchestrationService.createScenario(
                userAId,
                new CreateScenarioRequest(
                        "OTHER",
                        "de",
                        List.of(
                                new CreateScenarioRequest.ScenarioSentenceRequest("Ich bin hier", "I am here"),
                                new CreateScenarioRequest.ScenarioSentenceRequest("Du bist dort", "You are there")
                        )
                )
        );

        var sentenceToUpdate = saved.sentences().get(0);
        var updated = scenarioOrchestrationService.editScenario(
                userAId,
                saved.id(),
                new EditScenarioRequest(
                        "TALKING_FOR_LEARNING",
                        "de",
                        List.of(
                                new EditScenarioRequest.ScenarioSentenceUpdateRequest(
                                        sentenceToUpdate.id(),
                                        "Ich bin jetzt hier",
                                        "I am here now"
                                ),
                                new EditScenarioRequest.ScenarioSentenceUpdateRequest(
                                        null,
                                        "Wir lernen Deutsch",
                                        "We are learning German"
                                )
                        )
                )
        );

        assertThat(updated.sentences()).hasSize(2);
        assertThat(updated.nature()).isEqualTo("TALKING_FOR_LEARNING");
        assertThat(updated.sentences())
                .extracting(sentence -> sentence.sentence() + "|" + sentence.translation())
                .contains("Ich bin jetzt hier|I am here now", "Wir lernen Deutsch|We are learning German");
    }

    @Test
    public void fetchScenariosReturnsOnlyUserScenarios() {
        scenarioOrchestrationService.createScenario(
                userAId,
                new CreateScenarioRequest(
                        "TALKING_FOR_LEARNING",
                        "de",
                        List.of(new CreateScenarioRequest.ScenarioSentenceRequest("eins", "one"))
                )
        );
        scenarioOrchestrationService.createScenario(
                userBId,
                new CreateScenarioRequest(
                        "OTHER",
                        "de",
                        List.of(new CreateScenarioRequest.ScenarioSentenceRequest("zwei", "two"))
                )
        );

        var userAScenarios = scenarioOrchestrationService.fetchScenarios(userAId);

        assertThat(userAScenarios).hasSize(1);
        assertThat(scenarioEntityJpaRepo.findById(userAScenarios.get(0).id())).isPresent();
        assertThat(scenarioEntityJpaRepo.findById(userAScenarios.get(0).id()).get().getUserId()).isEqualTo(userAId);
        assertThat(userAScenarios.get(0).sentences())
                .extracting(sentence -> sentence.sentence())
                .containsExactly("eins");
    }

    @Test
    public void editScenarioRemovesSentenceWhenOmittedFromRequest() {
        var saved = scenarioOrchestrationService.createScenario(
                userAId,
                new CreateScenarioRequest(
                        "OTHER",
                        "de",
                        List.of(
                                new CreateScenarioRequest.ScenarioSentenceRequest("erste", "first"),
                                new CreateScenarioRequest.ScenarioSentenceRequest("zweite", "second")
                        )
                )
        );

        var remaining = saved.sentences().get(0);
        var updated = scenarioOrchestrationService.editScenario(
                userAId,
                saved.id(),
                new EditScenarioRequest(
                        "OTHER",
                        "de",
                        List.of(
                                new EditScenarioRequest.ScenarioSentenceUpdateRequest(
                                        remaining.id(),
                                        "erste aktualisiert",
                                        "first updated"
                                )
                        )
                )
        );

        assertThat(updated.sentences()).hasSize(1);
        assertThat(updated.sentences().get(0).sentence()).isEqualTo("erste aktualisiert");
        assertThat(updated.sentences().get(0).translation()).isEqualTo("first updated");
    }

    private void createUser(String userId, String username) {
        var user = new UserInfoEntity();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword("password");
        userInfoJpaRepo.save(user);
    }
}
