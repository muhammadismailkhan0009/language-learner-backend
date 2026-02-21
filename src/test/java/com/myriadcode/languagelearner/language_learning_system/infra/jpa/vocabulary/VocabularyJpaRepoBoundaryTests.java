package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class VocabularyJpaRepoBoundaryTests {

    @Test
    @DisplayName("Repo boundary save: writes schema version and display order before persistence")
    public void saveAssignsPersistenceSemanticsBeforeDelegation() {
        var handler = new VocabularyJpaRepoHandler();
        var adapter = new VocabularyJpaRepoImpl(handler.repo());

        var saved = adapter.save(sampleVocabulary("v1", "user-a"));

        assertThat(handler.lastSaved).isNotNull();
        assertThat(handler.lastSaved.getSchemaVersion()).isEqualTo(1);
        assertThat(handler.lastSaved.getExampleSentences()).hasSize(2);
        assertThat(handler.lastSaved.getExampleSentences().get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(handler.lastSaved.getExampleSentences().get(1).getDisplayOrder()).isEqualTo(1);
        assertThat(saved.userId().id()).isEqualTo("user-a");
    }

    @Test
    @DisplayName("Repo boundary findByIdAndUserId: enforces ownership filter")
    public void findByIdAndUserIdEnforcesOwnership() {
        var handler = new VocabularyJpaRepoHandler();
        var adapter = new VocabularyJpaRepoImpl(handler.repo());

        adapter.save(sampleVocabulary("v2", "user-a"));

        var sameUser = adapter.findByIdAndUserId("v2", "user-a");
        var otherUser = adapter.findByIdAndUserId("v2", "user-b");

        assertThat(sameUser).isPresent();
        assertThat(otherUser).isEmpty();
    }

    @Test
    @DisplayName("Repo boundary findByUserId: returns only scoped vocabulary")
    public void findByUserIdReturnsScopedData() {
        var handler = new VocabularyJpaRepoHandler();
        var adapter = new VocabularyJpaRepoImpl(handler.repo());

        adapter.save(sampleVocabulary("v3", "user-a"));
        adapter.save(sampleVocabulary("v4", "user-b"));

        var result = adapter.findByUserId("user-a");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id().id()).isEqualTo("v3");
        assertThat(result.get(0).userId().id()).isEqualTo("user-a");
    }

    private Vocabulary sampleVocabulary(String id, String userId) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(id),
                new UserId(userId),
                "auf jeden Fall",
                "definitely",
                Vocabulary.EntryKind.CHUNK,
                "agreement",
                List.of(
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("e1"),
                                "Auf jeden Fall komme ich.",
                                "I am definitely coming."
                        ),
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("e2"),
                                "Auf jeden Fall ja.",
                                "Definitely yes."
                        )
                )
        );
    }

    private static class VocabularyJpaRepoHandler implements InvocationHandler {

        private final Map<String, VocabularyEntity> store = new LinkedHashMap<>();
        private VocabularyEntity lastSaved;

        VocabularyEntityJpaRepo repo() {
            return (VocabularyEntityJpaRepo) Proxy.newProxyInstance(
                    VocabularyEntityJpaRepo.class.getClassLoader(),
                    new Class[]{VocabularyEntityJpaRepo.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            var methodName = method.getName();

            if ("save".equals(methodName)) {
                var entity = (VocabularyEntity) args[0];
                this.lastSaved = entity;
                store.put(entity.getId(), entity);
                return entity;
            }

            if ("findByIdAndUserId".equals(methodName)) {
                var id = (String) args[0];
                var userId = (String) args[1];
                var found = store.get(id);
                if (found == null || !userId.equals(found.getUserId())) {
                    return Optional.empty();
                }
                return Optional.of(found);
            }

            if ("findAllByUserId".equals(methodName)) {
                var userId = (String) args[0];
                var result = new ArrayList<VocabularyEntity>();
                for (VocabularyEntity entity : store.values()) {
                    if (userId.equals(entity.getUserId())) {
                        result.add(entity);
                    }
                }
                return result;
            }

            if ("toString".equals(methodName)) {
                return "VocabularyEntityJpaRepoProxy";
            }

            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }

            if ("equals".equals(methodName)) {
                return proxy == args[0];
            }

            throw new UnsupportedOperationException("Method not supported in test proxy: " + methodName);
        }
    }
}
