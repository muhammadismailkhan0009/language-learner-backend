package com.myriadcode.languagelearner.language_content.infra.llm.mappers;

import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;
import com.myriadcode.languagelearner.language_content.infra.llm.dtos.LLMVocabulary;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LLMVocabMapperTest {


    // We test your actual function via a minimal wrapper
    LLMVocabMapper mapper = LLMVocabMapper.INSTANCE;

    // ---------------------- TESTS ----------------------------

    @Test
    void testNullLLMInputReturnsEmpty() {
        var result = mapper.toDomainVocabulary(
                null
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testEmptyLLMInputReturnsEmpty() {
        var result = mapper.toDomainVocabulary(
                List.of()
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleLemmaSingleForm() {
        var llm = llm("gehen", Vocabulary.PartOfSpeech.VERB, "go", "gehe");

        var result = mapper.toDomainVocabulary(
                List.of(llm)
        );

        assertEquals(1, result.size());
        var vocab = result.get(0);

        assertEquals("gehen", vocab.root());
        assertEquals("go", vocab.translation());
        assertEquals(1, vocab.forms().size());
        assertEquals("gehe", vocab.forms().get(0).form());
    }

    @Test
    void testSingleLemmaMultipleForms() {
        var v1 = llm("gehen", Vocabulary.PartOfSpeech.VERB, "go", "gehe");
        var v2 = llm("gehen", Vocabulary.PartOfSpeech.VERB, "go", "geht");

        var result = mapper.toDomainVocabulary(
                List.of(v1, v2)
        );

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).forms().size());
    }

    @Test
    void testDuplicateFormMergesGrammarRoleAndNote() {
        var v1 = new LLMVocabulary(
                "gehen",
                Vocabulary.PartOfSpeech.VERB,
                "go",
                new LLMVocabulary.LLMWordForm("gehe", "subject", "first usage")
        );

        var v2 = new LLMVocabulary(
                "gehen",
                Vocabulary.PartOfSpeech.VERB,
                "go",
                new LLMVocabulary.LLMWordForm("gehe", "imperative", "second usage")
        );

        var result = mapper.toDomainVocabulary(
                List.of(v1, v2)
        );

        assertEquals(1, result.size());
        var forms = result.get(0).forms();

        // Only one form should exist
        assertEquals(1, forms.size());

        var form = forms.get(0);

        // Verify role merged
        assertTrue(form.grammaticalRole().contains("subject"));
        assertTrue(form.grammaticalRole().contains("imperative"));

        // Verify note merged
        assertTrue(form.note().contains("first usage"));
        assertTrue(form.note().contains("second usage"));
    }


    @Test
    void testDuplicateFormsInsideLLMOutputAreDeduped() {
        var v1 = llm("gehen", Vocabulary.PartOfSpeech.VERB, "go", "gehe");
        var v2 = llm("gehen", Vocabulary.PartOfSpeech.VERB, "go", "gehe"); // duplicate

        var result = mapper.toDomainVocabulary(
                List.of(v1, v2)
        );

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).forms().size());
        assertEquals("gehe", result.get(0).forms().get(0).form());
    }

    @Test
    void testMultipleLemmasAreSeparated() {
        var g1 = llm("gehen", Vocabulary.PartOfSpeech.VERB, "go", "gehe");
        var g2 = llm("machen", Vocabulary.PartOfSpeech.VERB, "do", "mache");

        var result = mapper.toDomainVocabulary(
                List.of(g1, g2)
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(v -> v.root().equals("gehen")));
        assertTrue(result.stream().anyMatch(v -> v.root().equals("machen")));
    }


    @Test
    void testStableOrdering() {
        var v1 = llm("gehen", Vocabulary.PartOfSpeech.VERB, "go", "gehe");
        var v2 = llm("machen", Vocabulary.PartOfSpeech.VERB, "do", "mache");
        var v3 = llm("sehen", Vocabulary.PartOfSpeech.VERB, "see", "sehe");

        var result = mapper.toDomainVocabulary(
                List.of(v1, v2, v3)
        );

        assertEquals("gehen", result.get(0).root());
        assertEquals("machen", result.get(1).root());
        assertEquals("sehen", result.get(2).root());
    }


    // ---------------------- Helper Methods ------------------------

    private LLMVocabulary llm(
            String root,
            Vocabulary.PartOfSpeech type,
            String translation,
            String form
    ) {
        return new LLMVocabulary(
                root,
                type,
                translation,
                new LLMVocabulary.LLMWordForm(form, "role", "note")
        );
    }


}