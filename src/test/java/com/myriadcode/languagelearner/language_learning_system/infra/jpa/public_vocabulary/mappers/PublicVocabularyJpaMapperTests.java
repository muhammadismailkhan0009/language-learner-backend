package com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.mappers;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicVocabularyJpaMapperTests {

    private static final PublicVocabularyJpaMapper MAPPER = PublicVocabularyJpaMapper.INSTANCE;

    @Test
    @DisplayName("Public JPA mapper: maps domain to entity semantics")
    public void toEntityMapsDomainValues() {
        var domain = new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId("pub-1"),
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.parse("2026-01-01T10:15:30Z")
        );

        var entity = MAPPER.toEntity(domain);

        assertThat(entity.getId()).isEqualTo("pub-1");
        assertThat(entity.getSourceVocabularyId()).isEqualTo("vocab-1");
        assertThat(entity.getPublishedByUserId()).isEqualTo("user-a");
        assertThat(entity.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("Public JPA mapper: maps entity to domain semantics")
    public void toDomainMapsEntityValues() {
        var entity = new com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.entities.PublicVocabularyEntity();
        entity.setId("pub-2");
        entity.setSourceVocabularyId("vocab-2");
        entity.setPublishedByUserId("user-b");
        entity.setStatus("UNPUBLISHED");
        entity.setPublishedAt(Instant.parse("2026-01-02T00:00:00Z"));

        var domain = MAPPER.toDomain(entity);

        assertThat(domain.id().id()).isEqualTo("pub-2");
        assertThat(domain.sourceVocabularyId().id()).isEqualTo("vocab-2");
        assertThat(domain.publishedByUserId().id()).isEqualTo("user-b");
        assertThat(domain.status()).isEqualTo(PublicVocabulary.PublicVocabularyStatus.UNPUBLISHED);
    }
}
