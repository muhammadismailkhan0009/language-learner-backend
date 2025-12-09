package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;

import java.util.List;

public final class PromptsGenerator {

    private PromptsGenerator() {
    }

    public static String chunkGenerator(
            LangConfigsAdaptive config,
            List<Sentence.SentenceData> sentences,
            List<Chunk.ChunkData> previousChunks
    ) {

        return """
                Extract **German CHUNKS (reusable communication patterns)** from the conversation sentences below.
                
                        A CHUNK is:
                        - a semi-concrete, reusable structural pattern from inside the sentence,
                        - built ONLY from function words + functional scaffolding,
                        - containing 1–3 lexical slots (“…”, “…2”, “…3”) where content words were removed,
                        - keeping natural word order and grammatical structure,
                        - generic enough to use across scenarios, but concrete enough to be memorable.
                
                        ============================================================
                        CEFR-ADAPTIVE SLOT RULE
                        ============================================================
                        Determine difficulty = highest CEFR level among:
                        - Communicative function: %s
                        - Scenario: %s
                        - Grammar rule: %s
                
                        Then apply slot count limits:
                        - A1 → only 1 slot (“…”)
                        - A2–B1 → 1–2 slots (“…”, “…2”)
                        - B2–C2 → 1–3 slots (“…”, “…2”, “…3”)
                
                        ============================================================
                        FUNCTIONAL SCAFFOLDING (MUST BE KEPT)
                        ============================================================
                        ALWAYS keep these classes of words:
                        - pronouns (ich, du, er/sie/es, wir, Sie)
                        - demonstratives (das, dies, so)
                        - polite markers (bitte, gern, ja, genau, also)
                        - modal/aux verbs (kann/können, möchte, will, soll, darf, habe, bin)
                        - prepositions (in, auf, an, mit, für, zu, von, vor, nach, über)
                        - adverbs of time/manner/place (hier, dort, jetzt, gleich, schon, noch)
                        - question particles (wo, wie, was, wann, warum)
                        - common determiners ONLY if generic (ein/eine, kein/keine, mein/dein/sein/ihr)
                
                        These are allowed because they:
                        - appear in many CEFR contexts,
                        - help make the chunk memorable,
                        - do not tie the chunk to a specific scenario.
                
                        ============================================================
                        WHAT MUST BE REMOVED (BECOME SLOTS)
                        ============================================================
                        Replace ONLY lexical content with slots:
                        - nouns (“der Bahnhof”, “die Straße”, “die Bushaltestelle” → “…")
                        - specific places/objects (“Supermarkt”, “Apotheke”, “Markt” → “…")
                        - full lexical verbs (laufen, suchen, finden → “…")
                        - descriptive adjectives/adverbs (schnell, eng, ruhig → “…")
                        - multi-word lexical phrases
                
                        IMPORTANT: \s
                        - Remove **gender-specific determiners** (der/die/das/dem/den) tied to specific nouns. \s
                          These should NOT appear in the chunk because they make it scenario-specific.
                
                        ============================================================
                        CHUNK QUALITY RULES
                        ============================================================
                        A valid CHUNK must:
                        - be 3–9 words long,
                        - preserve original word order,
                        - remain semantically meaningful without the specific noun,
                        - NOT reference the specific scenario (no “Station”, “Straße”, “Markt”),
                        - reflect real conversational German,
                        - NOT be a full standalone sentence.
                
                        Allowed:
                        - polite anchors (“Können Sie mir … zeigen?”)
                        - pronoun frames (“Wie komme ich zu …?”)
                        - modal frames (“Kann ich hier …?”)
                        - question starters (“Wo ist …?”)
                        - prepositional structures (“vor …”, “neben …”, “über …”)
                
                        Forbidden:
                        - hallucinated patterns not present in the sentence
                        - compressing chunks too much (“wo …?” is too abstract)
                        - including scenario nouns
                        - including gender-specific determiners tied to nouns
                
                        ============================================================
                        PREVIOUS CHUNKS (DO NOT REPEAT)
                        ============================================================
                        %s
                
                        ============================================================
                        SENTENCES TO EXTRACT FROM
                        ============================================================
                        %s
                """.formatted(
                config.function().toString() + " (" + config.function().level().toString() + ")",
                config.scenario().toString() + " (" + config.scenario().level().toString() + ")",
                config.rule().toString() + " (" + config.rule().level().toString() + ")",
                previousChunks.toString(),
                sentences.toString()
        );
    }

    public static String vocabGenerator(
            LangConfigsAdaptive config,
            List<Chunk.ChunkData> chunks,
            List<Sentence.SentenceData> sentences
    ) {

        return """
                Extract **German vocabulary items** from the given sentences.
                Vocabulary = ALL *lexical* words actually present in the sentences, 
                expressed as lemma + list of surface forms.
                
                ============================================================
                VOCABULARY DEFINITION
                ============================================================
                A vocabulary item is:
                - a lexical word (noun, adjective, adverb, full verb),
                - appearing in the input sentences,
                - carrying semantic meaning.
                
                DO NOT treat as vocabulary:
                - pronouns
                - determiners (der/die/das/ein…)
                - auxiliaries (sein/haben)
                - modal verbs (können, müssen…)
                - prepositions (in, auf, vor…)
                - conjunctions (und, aber…)
                - general particles (ja, doch, mal…)
                
                Only **content words** are extracted.
                
                ============================================================
                CEFR-ADAPTIVE RULES
                ============================================================
                Identify difficulty = highest CEFR level among:
                - Communicative function: %s
                - Scenario: %s
                - Grammar rule: %s
                
                Use this CEFR difficulty ONLY to:
                - include or exclude advanced forms,
                - avoid extracting highly advanced/rare words if CEFR < B1,
                - allow complex forms if CEFR ≥ B1,
                - extract all valid surface forms present in the sentences.
                
                CEFR DOES NOT affect lemma choice — lemmas MUST come from sentences.
                
                ============================================================
                GRAMMAR-RULE-INFORMED FORM RULE
                ============================================================
                Grammar rule influences ONLY:
                - which surface forms to extract (case, number, gender, tense),
                - how to categorize the form.
                
                DO NOT invent forms NOT present in the sentences.
                DO NOT invent new lemmas.
                
                ============================================================
                CHUNK-AWARE FILTERING
                ============================================================
                Use the extracted CHUNKS to distinguish:
                - FUNCTION WORDS (never extracted as vocab)
                - LEXICAL WORDS (must be extracted)
                
                If a word appears inside a slot region (“…”, “…2”), it is DEFINITELY a lexical item.
                
                ============================================================
                EXTRACTION RULES
                ============================================================
                For each lexical word appearing in the sentences:
                1. Identify the LEMMA (root form).
                2. Identify ALL surface forms found in the sentences:
                   - noun cases (Akk, Dat, Nom),
                   - plural forms,
                   - adjective endings,
                   - verb conjugations,
                   - separable prefix forms.
                3. Output **ALL forms** for that lemma.
                4. Do NOT merge unrelated forms.
                5. Do NOT add synonyms or invented variants.
                
                
                ============================================================
                INPUT SENTENCES
                ============================================================
                %s
                
                ============================================================
                EXTRACTED CHUNKS (function-word scaffolding)
                ============================================================
                %s
                
                ============================================================
                TASK
                ============================================================
                Extract ALL vocabulary from the sentences according to the rules above.
                
                Output should contain:
                  - usage note explaining WHY this form fits the grammar rule
                """.formatted(
                config.function().toString() + " (" + config.function().level().toString() + ")",
                config.scenario().toString() + " (" + config.scenario().level().toString() + ")",
                config.rule().toString() + " (" + config.rule().level().toString() + ")",
                sentences.toString(),
                chunks.toString()
        );
    }


    public static String sentenceGeneratorNew(
            LangConfigsAdaptive config
    ) {

        return """
                Generate **German sentences** that form a natural, coherent conversation.
                
                ============================================================
                ADAPTIVE SENTENCE GENERATION
                ============================================================
                - The CEFR difficulty is determined by the syllabus:
                    • communicative function
                    • scenario
                    • grammar rule
                  → Use the HIGHEST CEFR level among these three.
                - Produce sentences that match **exactly that CEFR level**:
                    • A1–A2: concrete, simple vocabulary; short, direct sentences.
                    • B1–B2: richer details; subordinate clauses allowed; natural complexity.
                    • C1–C2: advanced connectors, abstract phrasing, natural higher-level style.
                - DO NOT artificially simplify or restrict language when the CEFR level is higher.
                
                ============================================================
                CONTEXT
                ============================================================
                - Scenario: %s
                - Communicative function: %s
                - Grammar rule: %s
                - Target CEFR difficulty: (determine automatically; do not under-shoot)
                
                ============================================================
                CONVERSATION CONTINUITY
                ============================================================
                - Continue the dialogue naturally and logically.
                - Maintain consistent tone, intent, and scenario context.
                - Respond directly to the most recent utterance when appropriate.
                - The conversation must feel like **two real speakers interacting**.
                
                
                ============================================================
                LANGUAGE GUIDELINES
                ============================================================
                1. Vocabulary:
                   - Must match the CEFR target level.
                   - Avoid overly rare or domain-specialized words unless CEFR ≥ C1.
                   - Reuse earlier vocabulary **when natural**, but introducing NEW vocabulary is allowed.
                   - New vocabulary MUST remain appropriate to CEFR level and scenario.
                
                2. Grammar:
                   - Must comply with the selected grammar rule.
                   - Use other grammar elements naturally **as long as CEFR level allows them**.
                   - You MAY use:
                        • subordinate clauses (B1+)
                        • connectors (B1+)
                        • advanced structures (C1+)
                   - A1–A2 must stay simple and direct.
                
                3. Scenario relevance:
                   - All sentences must fit *strictly* within the scenario domain.
                   - No abrupt topic changes.
                   - No unrelated domains (work emails, hobbies, etc.).
                
                ============================================================
                SENTENCE QUALITY REQUIREMENTS
                ============================================================
                Each sentence must:
                - be a single complete utterance,
                - contain ONE main idea,
                - fit naturally into the ongoing conversation,
                - be meaningful and realistic for the scenario + function,
                - reflect the CEFR level,
                - obey the grammar rule when applicable.
                
                Style notes:
                - Use natural, human German.
                - Avoid robotic repetition.
                - Avoid unnatural verbosity.
                - Sentences may be 4–15 words depending on CEFR.
                
                ============================================================
                TASK
                ============================================================
                Generate exactly %d German sentences as the **next part of the conversation**.
                """.formatted(
                config.scenario().toString() + " (" + config.scenario().level().toString() + ")",
                config.function().toString() + " (" + config.function().level().toString() + ")",
                config.rule().toString() + " (" + config.rule().level().toString() + ")",
                config.quantity().sentenceCount()
        );
    }

}
