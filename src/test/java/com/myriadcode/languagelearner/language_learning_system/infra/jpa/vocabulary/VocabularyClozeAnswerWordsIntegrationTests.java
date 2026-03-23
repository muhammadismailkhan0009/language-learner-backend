package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyClozeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class VocabularyClozeAnswerWordsIntegrationTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private VocabularyRepo vocabularyRepo;

    @Autowired
    private VocabularyEntityJpaRepo vocabularyEntityJpaRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        vocabularyEntityJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("Vocabulary cloze answerWords: persists and reloads as JSON array text end to end")
    void clozeAnswerWordsRoundTripAsJsonArrayText() throws Exception {
        var vocabularyId = "vocab-cloze-1";
        var userId = "user-a";
        var expectedAnswerWords = List.of("immer", "noch");

        var vocabulary = new Vocabulary(
                new Vocabulary.VocabularyId(vocabularyId),
                new UserId(userId),
                "immer noch",
                "still",
                Vocabulary.EntryKind.CHUNK,
                "time marker",
                List.of(new VocabularyExampleSentence(
                        new VocabularyExampleSentence.VocabularyExampleSentenceId("ex-1"),
                        "Ich warte immer noch.",
                        "I am still waiting."
                )),
                new VocabularyClozeSentence(
                        new VocabularyClozeSentence.VocabularyClozeSentenceId("cloze-1"),
                        "Ich warte ____ ____.",
                        "time marker",
                        "Ich warte immer noch.",
                        expectedAnswerWords,
                        "I am still waiting."
                ),
                Instant.parse("2026-01-01T00:00:00Z")
        );

        vocabularyRepo.save(vocabulary);

        var reloaded = vocabularyRepo.findByIdAndUserId(vocabularyId, userId).orElseThrow();
        assertThat(reloaded.clozeSentence()).isNotNull();
        assertThat(reloaded.clozeSentence().answerWords()).containsExactlyElementsOf(expectedAnswerWords);

        var rawAnswerWordsJson = jdbcTemplate.queryForObject(
                "select answer_words_json from vocabulary_cloze_sentences where entry_id = ?",
                String.class,
                vocabularyId
        );
        assertThat(rawAnswerWordsJson).isNotBlank();
        assertThat(rawAnswerWordsJson).doesNotMatch("^\\d+$");

        var parsedFromDb = OBJECT_MAPPER.readValue(rawAnswerWordsJson, new TypeReference<List<String>>() {
        });
        assertThat(parsedFromDb).containsExactlyElementsOf(expectedAnswerWords);
    }
}
