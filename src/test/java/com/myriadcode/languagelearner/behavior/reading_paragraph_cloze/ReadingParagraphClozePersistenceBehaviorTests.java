package com.myriadcode.languagelearner.behavior.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.enums.ClozePracticeKind;
import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.*;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos.ReadingParagraphClozeSessionJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class ReadingParagraphClozePersistenceBehaviorTests {
    @Autowired ReadingParagraphClozeRepo repo;
    @Autowired ReadingParagraphClozeSessionJpaRepo jpaRepo;

    @AfterEach void clear() { jpaRepo.deleteAll(); }

    @Test
    void roundTripsExactFormsAndDeletesOnlyOwnedAggregate() {
        repo.save(session("s1", "u1", "{{b1}}", "ist gefahren"));
        repo.save(session("s2", "u1", "{{b2}}", "Kindern"));
        repo.save(session("s3", "u2", "{{b3}}", "weil"));

        var loaded = repo.findByIdAndUserId("s1", "u1").orElseThrow();
        var blank = loaded.paragraphs().getFirst().blanks().getFirst();
        assertThat(blank.exactAnswer()).isEqualTo("ist gefahren");
        assertThat(blank.grammarRuleIds()).containsExactly("g1");
        assertThat(repo.findAllByUserId("u1")).extracting(value -> value.id().id()).containsExactly("s2", "s1");

        assertThat(repo.deleteByIdAndUserId("s1", "u2")).isFalse();
        assertThat(repo.deleteByIdAndUserId("s1", "u1")).isTrue();
        assertThat(repo.findByIdAndUserId("s1", "u1")).isEmpty();
        assertThat(repo.findByIdAndUserId("s2", "u1")).isPresent();
        assertThat(repo.findByIdAndUserId("s3", "u2")).isPresent();
    }

    private ReadingParagraphClozeSession session(String id, String userId, String token, String answer) {
        var blank = new ReadingParagraphClozeBlank(
                new ReadingParagraphClozeBlank.ReadingParagraphClozeBlankId("blank-" + id), 0, token, answer,
                "Exact form explanation", ClozePracticeKind.VOCABULARY_AND_GRAMMAR,
                "v1", List.of("g1"));
        var paragraph = new ReadingParagraphClozeParagraph(
                new ReadingParagraphClozeParagraph.ReadingParagraphClozeParagraphId("paragraph-" + id), 0,
                "Scenario", "Mia " + token + ".", List.of(blank));
        return new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId(id), new UserId(userId),
                LanguageLevel.A2, Instant.parse("2026-01-01T00:00:00Z").plusSeconds(id.charAt(1)), List.of(paragraph));
    }
}
