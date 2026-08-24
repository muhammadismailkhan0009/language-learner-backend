package com.myriadcode.languagelearner.behavior.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.*;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.*;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class ReadingParagraphClozeServiceBehaviorTests {
    @Test
    void createsMultipleIndependentSessionsAndReturnsExactAnswer() {
        var repo = new MemoryRepo();
        var context = new ClozeParagraphGenerationContext(LanguageLevel.A2,
                List.of(new ClozeParagraphGenerationContext.VocabularySource("v1", "gehen", "go", "WORD", "note")), List.of());
        var contextService = new ReadingParagraphClozeGenerationContextService(null, null, null) {
            @Override public ClozeParagraphGenerationContext build(String userId, Integer limit) { return context; }
        };
        ClozeParagraphLlmApi llm = ignored -> new ClozeParagraphGeneration(List.of(
                new ClozeParagraphGeneration.Paragraph("Trip", "Mia {{b1}} nach Hause.", List.of(
                        new ClozeParagraphGeneration.Blank("{{b1}}", "geht", "Third-person singular.",
                                "VOCABULARY_FORM", "v1", List.of())))));
        FetchPrivateVocabularyApi vocabularyApi = new FetchPrivateVocabularyApi() {
            public com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord getVocabularyRecord(String id, String userId) { return null; }
            public List<com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord> getVocabularyRecords(List<String> ids, String userId) { return List.of(); }
        };
        var grammarRepo = new EmptyGrammarRepo();
        var service = new ReadingParagraphClozeService(repo, contextService, llm, vocabularyApi, grammarRepo);

        var first = service.createSession("u1", 10);
        var second = service.createSession("u1", 10);

        assertThat(first.sessionId()).isNotEqualTo(second.sessionId());
        assertThat(service.listSessions("u1")).hasSize(2);
        assertThat(first.paragraphs().getFirst().blanks().getFirst().exactAnswer()).isEqualTo("geht");
    }

    private static final class EmptyGrammarRepo implements GrammarRuleRepo {
        public com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule save(com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule rule) { return rule; }
        public Optional<com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule> findById(String id) { return Optional.empty(); }
        public List<com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule> findAll() { return List.of(); }
        public List<com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule> findByStatus(String status) { return List.of(); }
        public void deleteById(String id) {}
    }

    private static final class MemoryRepo implements ReadingParagraphClozeRepo {
        private final List<ReadingParagraphClozeSession> sessions = new ArrayList<>();
        public ReadingParagraphClozeSession save(ReadingParagraphClozeSession session) { sessions.add(session); return session; }
        public List<ReadingParagraphClozeSession> findAllByUserId(String userId) { return sessions.stream().filter(s -> s.userId().id().equals(userId)).toList(); }
        public Optional<ReadingParagraphClozeSession> findByIdAndUserId(String id, String userId) { return sessions.stream().filter(s -> s.id().id().equals(id) && s.userId().id().equals(userId)).findFirst(); }
        public boolean deleteByIdAndUserId(String id, String userId) { return sessions.removeIf(s -> s.id().id().equals(id) && s.userId().id().equals(userId)); }
    }
}
