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
            LangConfigsAdaptive config,
            List<Sentence.SentenceData> previousSentences
    ) {

        return """
        Generate **German dialogue units** that form a natural, coherent
        learning conversation.

        ============================================================
        ROLE & GOAL
        ============================================================
        You are generating the **next batch of dialogue units**
        in an ongoing conversation.

        A batch is a **continuation segment**, not a restart
        and not a casual chat log.

        Your goal is to:
        - continue naturally from the previous dialogue,
        - introduce **new, relevant information** driven by the inputs,
        - avoid repetition, filler, and low-information turns,
        - keep the dialogue realistic but optimized for learning.

        ============================================================
        CEFR & SYLLABUS CONTROL
        ============================================================
        - Scenario: %s
        - Scenario Context (Group): %s
        - Communicative function: %s
        - Grammar rule: %s

        - The CEFR difficulty is determined by the syllabus inputs:
            • scenario
            • communicative function
            • grammar rule
          → Use the HIGHEST CEFR level among these.

        - Language output must match **exactly that CEFR level**:
            • A1–A2: simple, concrete, everyday language.
            • B1–B2: richer detail and variation.
            • C1–C2: advanced, natural expression.

        ============================================================
        PREVIOUS CONVERSATION
        ============================================================
        The following dialogue units have **already been generated**.

        Treat them as **progressive conversation history**, not inspiration.

        Previous dialogue:
        %s
        
        ============================================================
        INTERNAL GENERATION STAGES (MANDATORY)
        ============================================================

        ------------------------------------------------------------
        STAGE 1 — Extract conversation state
        ------------------------------------------------------------
        Analyze the previous dialogue as **irreversible state**.

        Identify:
        - which information dimensions are already covered
          (e.g. greeting, identity, origin, residence, work/study,
           hobbies, daily life, preferences),
        - which exchanges appear complete,
        - which speakers are present.

        Assume all stated information is known to all speakers.
        Do NOT reintroduce completed dimensions unless required
        by the communicative function.

        ------------------------------------------------------------
        STAGE 2 — Select next conversational focus
        ------------------------------------------------------------
        Select the next conversational focus strictly from
        what is **NOT yet covered**.

        The next focus must:
        - follow logically from the previous dialogue,
        - remain within the same scenario context,
        - be appropriate for the communicative function.

        Do NOT:
        - re-ask questions whose answers already exist,
        - reintroduce the same information dimension,
        - repeat the same dimension for multiple speakers
          without adding new meaning.

        ------------------------------------------------------------
        STAGE 3 — Plan dialogue progression
        ------------------------------------------------------------
        Plan a short sequence of dialogue units where:
        - each unit depends on the previous one,
        - questions introduce new information only,
        - answers add concrete content,
        - the dialogue moves forward conceptually.

        Avoid:
        - filler-only reactions,
        - acknowledgement-only turns,
        - mechanical back-and-forth symmetry,
        - restarting earlier dialogue patterns.
        - asking or repeating same dialogue unit for speakers.
        
        For any single information dimension:
         - introduce it,
         - optionally clarify or exemplify it,
         - then move on.
        
        Do NOT exhaust the same dimension with repeated follow-up questions within the same batch.
        

        ------------------------------------------------------------
        STAGE 4 — Generate dialogue units
        ------------------------------------------------------------
        Generate the dialogue units according to the plan.

        ============================================================
        DEFINITION — DIALOGUE UNIT
        ============================================================
        A dialogue unit is a meaning-bearing conversational turn.

        A valid dialogue unit MUST:
        - introduce at least one concrete piece of information
          (fact, question, preference, action, habit, reason),
        - NOT consist solely of greetings, acknowledgements,
          confirmations, or emotional reactions,
        - NOT be a single-word or formula-only utterance.

        The following are NOT valid dialogue units on their own:
        - greetings only (“Hallo!”, “Guten Tag!”),
        - acknowledgements only (“Ah”, “Oh”, “Interessant”),
        - politeness-only reactions (“Das ist gut”, “Schön”),
        - mirrored confirmations without new information.

        ============================================================
        DIALOGUE UNIT CONSTRAINTS
        ============================================================
        Each dialogue unit must:
        - be a complete German utterance,
        - belong to a single speaker,
        - be prefixed with the speaker name followed by a colon
          (e.g. “Max: …”, “Anna: …”),
        - contain ONE primary communicative intent,
        - add new meaning or information,
        - fit the CEFR level,
        - obey the grammar rule,
        - stay within the scenario context.

        A dialogue unit that does not add new information
        is considered invalid.

        ============================================================
        CONVERSATION CONSTRAINTS
        ============================================================
        - A conversation must not have same sentences back-and-forth. In other words, avoid "ping-pong" 
        dialogue units.
        
        ============================================================
        SPEAKER CONSTRAINTS
        ============================================================
        - A speaker can only be either "Max" or "Anna".
        - Speaker name must not influence dialogue at all except the use of gender-based vocabulary and chunks.
        - A speaker name is only there for identification and visualization.
        - A speaker must not introduce personality or any other changes in dialogue.
        
        Prefer:
        - information-dense utterances,
        - natural phrasing,
        - concise but meaningful dialogue units.
        
        Expressions that merely acknowledgeor positively evaluate previous information
        without adding new content(e.g. “Das ist gut”, “Interessant”) 
        are not valid dialogue units.
        
        ============================================================
        OUTPUT RULES
        ============================================================
        - Generate exactly %d German dialogue units.
        - Each unit must include an explicit speaker label.
        - Do NOT include explanations, labels, or analysis.
        - Output only the dialogue units, in order.
        """.formatted(
                config.scenario().toString() + " (" + config.scenario().level().toString() + ")",
                config.scenario().group().toString(),
                config.function().toString() + " (" + config.function().level().toString() + ")",
                config.rule().toString() + " (" + config.rule().level().toString() + ")",
                previousSentences,
                config.quantity().sentenceCount()
        );
    }

}
