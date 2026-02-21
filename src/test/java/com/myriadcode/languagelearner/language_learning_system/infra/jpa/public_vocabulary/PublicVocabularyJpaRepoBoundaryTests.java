package com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.entities.PublicVocabularyEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.repos.PublicVocabularyEntityJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicVocabularyJpaRepoBoundaryTests {

    @Test
    @DisplayName("Public repo boundary save: persists mapped status and source vocabulary reference")
    public void savePersistsMappedFields() {
        var handler = new PublicVocabularyJpaRepoHandler();
        var adapter = new PublicVocabularyJpaRepoImpl(handler.repo());

        var saved = adapter.save(samplePublic("pub-1", "vocab-1", "user-a", PublicVocabulary.PublicVocabularyStatus.PUBLISHED));

        assertThat(handler.lastSaved).isNotNull();
        assertThat(handler.lastSaved.getSourceVocabularyId()).isEqualTo("vocab-1");
        assertThat(handler.lastSaved.getStatus()).isEqualTo("PUBLISHED");
        assertThat(saved.status()).isEqualTo(PublicVocabulary.PublicVocabularyStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Public repo boundary findBySourceVocabularyId: delegates and maps optional")
    public void findBySourceVocabularyIdMapsOptional() {
        var handler = new PublicVocabularyJpaRepoHandler();
        var adapter = new PublicVocabularyJpaRepoImpl(handler.repo());
        adapter.save(samplePublic("pub-2", "vocab-2", "user-a", PublicVocabulary.PublicVocabularyStatus.PUBLISHED));

        var found = adapter.findBySourceVocabularyId("vocab-2");

        assertThat(found).isPresent();
        assertThat(found.get().id().id()).isEqualTo("pub-2");
    }

    @Test
    @DisplayName("Public repo boundary findAllByStatus: returns status-filtered records ordered by publishedAt desc")
    public void findAllByStatusFiltersAndOrders() {
        var handler = new PublicVocabularyJpaRepoHandler();
        var adapter = new PublicVocabularyJpaRepoImpl(handler.repo());
        adapter.save(samplePublic("pub-3", "vocab-3", "user-a", PublicVocabulary.PublicVocabularyStatus.PUBLISHED));
        adapter.save(samplePublic("pub-4", "vocab-4", "user-a", PublicVocabulary.PublicVocabularyStatus.UNPUBLISHED));

        var published = adapter.findAllByStatus(PublicVocabulary.PublicVocabularyStatus.PUBLISHED);

        assertThat(published).hasSize(1);
        assertThat(published.get(0).id().id()).isEqualTo("pub-3");
    }

    private PublicVocabulary samplePublic(String id,
                                          String sourceVocabularyId,
                                          String userId,
                                          PublicVocabulary.PublicVocabularyStatus status) {
        return new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId(id),
                new Vocabulary.VocabularyId(sourceVocabularyId),
                new UserId(userId),
                status,
                Instant.now()
        );
    }

    private static class PublicVocabularyJpaRepoHandler implements InvocationHandler {

        private final Map<String, PublicVocabularyEntity> store = new LinkedHashMap<>();
        private PublicVocabularyEntity lastSaved;

        PublicVocabularyEntityJpaRepo repo() {
            return (PublicVocabularyEntityJpaRepo) Proxy.newProxyInstance(
                    PublicVocabularyEntityJpaRepo.class.getClassLoader(),
                    new Class[]{PublicVocabularyEntityJpaRepo.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            var name = method.getName();

            if ("save".equals(name)) {
                var entity = (PublicVocabularyEntity) args[0];
                lastSaved = entity;
                store.put(entity.getId(), entity);
                return entity;
            }

            if ("findBySourceVocabularyId".equals(name)) {
                var sourceId = (String) args[0];
                return store.values().stream()
                        .filter(entity -> sourceId.equals(entity.getSourceVocabularyId()))
                        .findFirst();
            }

            if ("findAllByStatusOrderByPublishedAtDesc".equals(name)) {
                var status = (String) args[0];
                return store.values().stream()
                        .filter(entity -> status.equals(entity.getStatus()))
                        .sorted(Comparator.comparing(PublicVocabularyEntity::getPublishedAt).reversed())
                        .toList();
            }

            if ("toString".equals(name)) {
                return "PublicVocabularyEntityJpaRepoProxy";
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }

            throw new UnsupportedOperationException("Unsupported method in test proxy: " + name);
        }
    }
}
