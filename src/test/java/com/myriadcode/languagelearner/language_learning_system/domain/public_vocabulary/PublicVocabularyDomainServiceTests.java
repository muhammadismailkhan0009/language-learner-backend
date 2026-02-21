package com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.services.PublicVocabularyDomainService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PublicVocabularyDomainServiceTests {

    @Test
    @DisplayName("Publish public vocabulary: creates published record with ids and timestamp")
    public void publishCreatesRecord() {
        var published = PublicVocabularyDomainService.publish(
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a")
        );

        assertThat(published.id().id()).isNotBlank();
        assertThat(published.sourceVocabularyId().id()).isEqualTo("vocab-1");
        assertThat(published.publishedByUserId().id()).isEqualTo("user-a");
        assertThat(published.status()).isEqualTo(PublicVocabulary.PublicVocabularyStatus.PUBLISHED);
        assertThat(published.publishedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Publish public vocabulary: fails when source vocabulary id is missing")
    public void publishFailsWhenSourceIdMissing() {
        assertThatThrownBy(() -> PublicVocabularyDomainService.publish(
                new Vocabulary.VocabularyId(" "),
                new UserId("user-a")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source vocabulary id is required");
    }

    @Test
    @DisplayName("Ensure published: converts unpublished record to published")
    public void ensurePublishedConvertsStatus() {
        var unpublished = new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId("pub-1"),
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                PublicVocabulary.PublicVocabularyStatus.UNPUBLISHED,
                Instant.now().minusSeconds(60)
        );

        var republished = PublicVocabularyDomainService.ensurePublished(unpublished);

        assertThat(republished.id().id()).isEqualTo("pub-1");
        assertThat(republished.status()).isEqualTo(PublicVocabulary.PublicVocabularyStatus.PUBLISHED);
        assertThat(republished.publishedAt()).isAfter(unpublished.publishedAt());
    }
}
