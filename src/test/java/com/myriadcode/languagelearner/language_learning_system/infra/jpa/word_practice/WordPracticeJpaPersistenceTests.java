package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class WordPracticeJpaPersistenceTests {
    @Autowired WordPracticeRepo practices;
    @Autowired JdbcClient jdbc;

    @BeforeEach
    void seedVocabulary() {
        var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        for (String id : List.of("v1", "v2")) {
            jdbc.sql("""
                    insert into vocabulary_entries
                        (id, user_id, surface, translation, entry_kind, schema_version, created_at, updated_at)
                    values (:id, 'u1', :surface, :translation, 'WORD', 1, :now, :now)
                    """)
                    .param("id", id)
                    .param("surface", "Wort " + id)
                    .param("translation", "word " + id)
                    .param("now", now)
                    .update();
        }
    }

    @AfterEach
    void clean() {
        jdbc.sql("delete from word_practice_consumed_weak_events").update();
        jdbc.sql("delete from word_practices").update();
        jdbc.sql("delete from vocabulary_entries where id in ('v1', 'v2')").update();
    }

    @Test
    void storesAllPracticesAndConsumesWeakEventWithoutHistoricalGenerationState() {
        var createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        var selected = List.of(
                new WordPracticeCandidate("v1", WordPracticeSelectionCategory.WEAK, "weak-1"),
                new WordPracticeCandidate("v2", WordPracticeSelectionCategory.NEW, null));
        var groups = List.of(group("v1", 3), group("v2", 4));

        practices.saveGenerationAndConsumeWeakEvents("u1", selected, groups, createdAt);

        assertThat(practices.findByUserId("u1")).hasSize(7);
        assertThat(practices.countDistinctActiveVocabulary("u1")).isEqualTo(2);
        assertThat(practices.findActiveVocabularyIds("u1")).containsExactlyInAnyOrder("v1", "v2");
        assertThat(jdbc.sql("select weak_event_id from word_practice_consumed_weak_events where user_id='u1'")
                .query(String.class).single()).isEqualTo("weak-1");
    }

    @Test
    void deletingCorrectPracticeReleasesVocabularyOnlyAfterItsLastPractice() {
        practices.saveGenerationAndConsumeWeakEvents("u1",
                List.of(new WordPracticeCandidate("v1", WordPracticeSelectionCategory.NEW, null)),
                List.of(group("v1", 3)), Instant.now());
        var stored = practices.findByUserId("u1");

        practices.deleteByIdAndUserId(stored.get(0).id(), "u1");
        practices.deleteByIdAndUserId(stored.get(1).id(), "u1");
        assertThat(practices.countDistinctActiveVocabulary("u1")).isOne();

        practices.deleteByIdAndUserId(stored.get(2).id(), "u1");
        assertThat(practices.countDistinctActiveVocabulary("u1")).isZero();
    }

    private GeneratedWordPracticeGroup group(String id, int size) {
        var rows = java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> new GeneratedWordPractice(id,
                        index % 2 == 0 ? WordPracticeDirection.GERMAN_TO_ENGLISH : WordPracticeDirection.ENGLISH_TO_GERMAN,
                        "source " + index, "cloze " + index, "complete " + index, "answer " + index,
                        List.of("accepted " + index)))
                .toList();
        return new GeneratedWordPracticeGroup(id, rows);
    }
}
