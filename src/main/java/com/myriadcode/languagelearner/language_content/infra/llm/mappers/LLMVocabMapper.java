package com.myriadcode.languagelearner.language_content.infra.llm.mappers;

import com.myriadcode.languagelearner.language_content.domain.model.Vocabulary;
import com.myriadcode.languagelearner.language_content.infra.llm.dtos.LLMVocabulary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mapper
public interface LLMVocabMapper {
    LLMVocabMapper INSTANCE = Mappers.getMapper(LLMVocabMapper.class);


    @Mapping(target = "wordForm", source = "form")
    LLMVocabulary.LLMWordForm toLLMForm(Vocabulary.WordForm wordForm);

    default List<LLMVocabulary> toLLMVocabulary(List<Vocabulary.VocabularyData> domain) {
        if (domain == null || domain.isEmpty()) {
            return List.of();
        }

        return domain.stream()
                .flatMap(v -> {
                    if (v.forms() == null || v.forms().isEmpty()) {
                        return Stream.empty();
                    }

                    return v.forms().stream()
                            .map(f -> new LLMVocabulary(
                                    v.root(),
                                    v.type(),
                                    v.translation(),
                                    toLLMForm(f) // WordForm → LLMWordForm (MapStruct)
                            ));
                })
                .toList();
    }


    default List<Vocabulary.VocabularyData> toDomainVocabulary(
            List<LLMVocabulary> llmVocabulary
    ) {

        if (llmVocabulary == null || llmVocabulary.isEmpty()) {
            return List.of();
        }

        // (lemma → list of WordForms)
        Map<String, List<Vocabulary.WordForm>> formsByLemma = new LinkedHashMap<>();

        // (lemma → metadata)
        Map<String, LLMVocabulary> lemmaMeta = new LinkedHashMap<>();

        for (LLMVocabulary llm : llmVocabulary) {

            String lemmaKey = llm.root() + "|" + llm.type();
            String formString = llm.form().wordForm();

            // Create entry if missing
            lemmaMeta.putIfAbsent(lemmaKey, llm);
            formsByLemma.computeIfAbsent(lemmaKey, k -> new ArrayList<>());

            List<Vocabulary.WordForm> formList = formsByLemma.get(lemmaKey);

            // Check if this form already exists
            Vocabulary.WordForm existing = formList.stream()
                    .filter(f -> f.form().equals(formString))
                    .findFirst()
                    .orElse(null);

            // If the form already exists → merge grammar info
            if (existing != null) {

                String mergedRole = merge(existing.grammaticalRole(), llm.form().grammaticalRole());
                String mergedNote = merge(existing.note(), llm.form().note());

                Vocabulary.WordForm merged = new Vocabulary.WordForm(
                        existing.form(),
                        mergedRole,
                        mergedNote
                );

                // Replace the old version
                formList.remove(existing);
                formList.add(merged);
                continue;
            }

            // Otherwise → create new WordForm
            Vocabulary.WordForm newForm = new Vocabulary.WordForm(
                    formString,
                    llm.form().grammaticalRole(),
                    llm.form().note()
            );

            formList.add(newForm);
        }

        // Build final vocabulary list
        List<Vocabulary.VocabularyData> result = new ArrayList<>();

        for (var entry : formsByLemma.entrySet()) {
            LLMVocabulary meta = lemmaMeta.get(entry.getKey());
            result.add(new Vocabulary.VocabularyData(
                    meta.root(),
                    meta.type(),
                    meta.translation(),
                    entry.getValue()
            ));
        }

        return result;
    }

    // Utility: merge "existing" + "new" safely
    private String merge(String existing, String incoming) {
        if (incoming == null || incoming.isBlank()) return existing;
        if (existing == null || existing.isBlank()) return incoming;
        if (existing.equals(incoming)) return existing;
        return existing + " | " + incoming;
    }


}
