package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.domain.model.Chunk;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentInput;

import java.util.List;

public final class PromptsGenerator {

  private PromptsGenerator() {
  }

  public static String chunkGenerator(
      LangConfigsAdaptive config,
      List<Sentence.SentenceData> sentences,
      List<Chunk.ChunkData> previousChunks) {

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
        sentences.toString());
  }

  public static String vocabGenerator(
      LangConfigsAdaptive config,
      List<Chunk.ChunkData> chunks,
      List<Sentence.SentenceData> sentences) {

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
        chunks.toString());
  }

  private static String formatVocabulary(List<ReadingPracticeVocabularySeed> vocabulary) {
    StringBuilder builder = new StringBuilder();

    for (ReadingPracticeVocabularySeed v : vocabulary) {
      builder.append(v.surface())
          .append(" - ")
          .append(v.translation())
          .append("\n");
    }

    return builder.toString();
  }

  public static String readingTopicSelection(
      List<ReadingPracticeVocabularySeed> vocabulary,
      List<String> previousTopics,
      LanguageLevel difficultyLevel) {

    String vocabList = formatVocabulary(vocabulary);
    String topics = previousTopics == null || previousTopics.isEmpty()
        ? "(none)"
        : String.join("\n", previousTopics);

    return """
        Act as an expert German language teacher. You select the best single topic for a German reading exercise.

        CEFR level: %s

        Goal:
        Pick ONE topic that allows the learner to naturally use the given vocabulary.

        Rules:
        - Return EXACTLY 1 topic.
        - The topic must be a SHORT to MEDIUM PHRASE (4–20 words).
        - The topic must describe a real-life situation or experience.
        - Do NOT write a full sentence.
        - The topic must be suitable for a short to medium reading exercise.
        - Avoid repeating or closely paraphrasing recent topics.

        Steps:
        1- Create 5 topics as candidate.
        2- Decide the most suitable topic among candidates.

        Recent topics to avoid:
        %s

        Learner Vocabulary (German - translation):
        %s
        """.formatted(difficultyLevel, topics, vocabList);
  }

  public static String readingContentParagraphs(
    String topic,
    List<ReadingPracticeVocabularySeed> vocabulary,
    LanguageLevel difficultyLevel,
    List<String> grammarRuleTitles) {

  String vocabList = formatVocabulary(vocabulary);
  String grammarTitles = grammarRuleTitles == null || grammarRuleTitles.isEmpty()
      ? "(none provided)"
      : grammarRuleTitles.stream()
          .filter(title -> title != null && !title.isBlank())
          .map(title -> "- " + title.trim())
          .collect(java.util.stream.Collectors.joining("\n"));

  return """
      Act as an expert German language teacher.
      Generate natural German reading and listening practice material.

      CEFR Level: %s
      Topic: "%s"

      Primary Goal:
      Create natural, coherent, idiomatic German at the requested CEFR level.
      The text should help the learner repeatedly encounter useful known vocabulary
      in meaningful context without sounding artificial or repetitive.

      Priority:
      Follow this priority order:
      1. Natural and idiomatic German.
      2. Clear and coherent progression of meaning.
      3. Appropriate difficulty for the requested CEFR level.
      4. Useful reuse of learner vocabulary.
      5. Natural exposure to suitable grammar structures.

      Coherence and Progression:
      - Build the text around a real situation, event, experience, conversation,
        or connected sequence of thoughts.
      - Sentences must connect logically.
      - Each sentence should normally add new information or move the situation forward.
      - Do not repeat the same idea merely using slightly different wording.
      - When appropriate, give the text a clear beginning, development, and ending.
      - The result must read like a small genuine text, not like vocabulary drills
        joined together.

      Vocabulary Usage:
      - Select only the provided learner vocabulary that fits the topic naturally.
      - Do NOT try to include most or all provided vocabulary.
      - Natural German is more important than vocabulary coverage.
      - Reuse useful target vocabulary when it naturally belongs in multiple sentences.
      - A selected vocabulary item may normally appear around 1-3 times.
      - More repetition is allowed only when it is genuinely natural in the situation.
      - Never repeat an idea solely to create another occurrence of a target word.
      - Never use awkward expressions, unusual nominalizations, or unnatural sentence
        constructions merely to include supplied vocabulary.
      - Use natural inflected forms of vocabulary where appropriate.
      - Multiple learner vocabulary items may occur in the same sentence when natural.

      Additional Vocabulary:
      - The supplied learner vocabulary should guide the content, but it must not
        restrict all words in the text.
      - Freely use common everyday vocabulary appropriate to the requested CEFR level
        when needed for natural and coherent German.
      - Avoid unnecessary rare, literary, technical, or highly specialized words.
      - Do not deliberately introduce large amounts of new thematic vocabulary.

      Grammar Usage:
      - Use natural German grammar appropriate to the requested CEFR level.
      - Eligible grammar-rule titles are provided below.
      - Choose only grammar rules that fit the topic and text naturally.
      - You do not need to use every eligible grammar rule.
      - Prefer approximately 1-3 relevant grammar structures in a text when suitable.
      - An important grammar structure may naturally recur several times for reinforcement.
      - Do not force a grammar structure into every sentence.
      - Do not avoid useful repetition of grammar merely for the sake of structural variety.
      - Ordinary CEFR-appropriate grammar may also be used even when it is not explicitly
        listed among the eligible grammar-rule titles.
      - Questions, modal verbs, negation, subordinate clauses, connectors, separable verbs,
        perfect tense, and other appropriate structures may occur naturally according
        to the learner level.

      Sentence Difficulty:
      Adapt sentence complexity to the requested CEFR level.

      - A1:
        Usually about 5-10 words per sentence.
        Prefer simple main clauses and very common structures.

      - A2:
        Usually about 6-14 words per sentence.
        Allow common connectors, modal verbs, perfect tense, subordinate clauses,
        and moderately varied word order.

      - B1:
        Usually about 8-18 words per sentence.
        Allow connected clauses, subordinate clauses, varied word order,
        explanations, reasons, conditions, and more developed thoughts.

      These are guidelines rather than strict limits.
      Occasional shorter or longer sentences are allowed when natural.

      Paragraph Guidelines:
      - Minimum paragraphs: 1
      - Maximum paragraphs: 2
      - Usually 4-7 sentences per paragraph.
      - Usually 5-12 sentences total.
      - Keep paragraphs focused and internally coherent.
      - Start a second paragraph only when there is a meaningful change in time,
        subtopic, perspective, or stage of the situation.

      Style:
      - Use clear, contemporary, everyday German.
      - Prefer language that a German speaker could naturally say or write.
      - Avoid textbook-like semantic repetition.
      - Avoid producing a sequence of nearly identical sentence structures.
      - Do not include explanations, translations, headings, bullet points, or numbering.
      - Output only the German practice text requested by the response schema.

      Learner Vocabulary (German - translation):
      %s

      Eligible Grammar-Rule Titles:
      %s
      """.formatted(
          difficultyLevel,
          topic,
          vocabList,
          grammarTitles);
}

public static String clozeParagraph(
  com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGenerationContext context) {

return """
    Create coherent German cloze-practice paragraphs for a learner at CEFR level %s.

    PURPOSE

    These are focused production exercises.

    The learner must generate the exact German form required by each blank from context.

    Practice may target:

    1. VOCABULARY_FORM
       Productive control of supplied vocabulary:
       - verb conjugation
       - separable-verb forms
       - participles
       - noun number
       - article/case forms
       - adjective forms
       - other natural inflected forms

    2. GRAMMAR
       Application of one or more explicitly supplied grammar rules in context.

    3. VOCABULARY_AND_GRAMMAR
       A supplied vocabulary item whose correct use deliberately requires one or more
       supplied grammar rules.

    The supplied vocabulary belongs to the learner's productive/writing vocabulary.
    Prefer it for target blanks.

    Supporting German vocabulary may be added when necessary for natural, coherent German,
    provided it is appropriate for the learner's CEFR level.

    Supporting vocabulary must not become a vocabulary target unless it is present in the
    supplied vocabulary sources.

    PARAGRAPH DESIGN

    Each paragraph is shown and solved independently.

    Therefore every paragraph must stand alone as a complete mini-situation:
    - do not depend on information from another paragraph
    - do not continue a story from another paragraph
    - establish enough context inside the paragraph itself
    - have a meaningful scenarioLabel
    - contain a small but understandable progression, event, problem, decision, or activity

    Normally use:
    - 4-6 sentences per paragraph
    - 3-4 blanks per paragraph
    - never more than 4 blanks

    Do not make every sentence contain a blank.
    Leave enough complete German visible for the learner to understand the situation.

    Multiple paragraphs may cover different situations.

    When supplied targets do not fit naturally into one coherent situation, prefer separate
    self-contained paragraphs rather than forcing unrelated vocabulary into one story.

    GENERAL PRIORITIES

    1. Natural and idiomatic German.
    2. Clear and coherent mini-situation.
    3. Exactly one intended correct answer for every blank.
    4. Appropriate CEFR difficulty.
    5. Useful productive practice.
    6. Natural and reasonably diverse use of supplied vocabulary and grammar.

    Do not force all supplied vocabulary or grammar rules.
    Choose only targets that can be practiced naturally.

    Across multiple paragraphs, prefer useful diversity of vocabulary and situations rather
    than repeatedly using the same few words.

    VOCABULARY PRACTICE

    Vocabulary-based blanks may use either of two cue styles.

    A. FORM-ONLY CUE

    Give the supplied German base entry as the cue.

    Example:

    Anna {{blank-1}} den Zug. (sehen)

    exactAnswer:
    sieht

    This tests:

    known German base form
    -> correct contextual German form

    B. RETRIEVAL-AND-FORM CUE

    Give the supplied English meaning as the cue.

    Example:

    Anna {{blank-2}} den Zug. (to see)

    exactAnswer:
    sieht

    This tests:

    English meaning
    -> German lexical item
    -> correct contextual German form

    Use both cue styles when suitable.

    Prefer FORM-ONLY when the main challenge should be producing the correct form.

    Prefer RETRIEVAL-AND-FORM when productive lexical retrieval should also be practiced.

    Do not use multiple choice.

    The cue must identify the intended vocabulary clearly enough that another common German
    word would not be an equally reasonable answer.

    For vocabulary targets:
    - vocabularyId must refer to the supplied vocabulary entry being practiced
    - exactAnswer must be the actual German surface form required in context
    - exactAnswer does not need to equal the supplied base form

    Example:

    supplied entry:
    sehen

    sentence:
    Anna {{blank-3}} den Zug. (sehen)

    exactAnswer:
    sieht

    GRAMMAR PRACTICE

    Grammar-based blanks must deliberately exercise supplied grammar rules.

    Prefer focused grammar practice rather than combining many unrelated grammar concepts
    inside one paragraph.

    Rules that naturally belong together may appear together.

    Example target:
    masculine accusative

    Anna sieht {{blank-4}}. (der Zug)

    exactAnswer:
    den Zug

    The cue must provide enough lexical information that the learner is primarily solving
    the intended grammar problem.

    grammarRuleIds must contain only supplied grammar rules genuinely required for that blank.

    Do not attach a grammar rule merely because it happens to occur somewhere in the sentence.

    PRACTICE KIND

    VOCABULARY_FORM:
    - primary target is productive retrieval or form generation of a supplied vocabulary item
    - requires vocabularyId
    - grammarRuleIds must be empty

    GRAMMAR:
    - primary target is application of supplied grammar rule(s)
    - vocabularyId must be null
    - requires at least one grammarRuleId

    VOCABULARY_AND_GRAMMAR:
    - productive vocabulary use and supplied grammar rule(s) are both deliberate targets
    - requires vocabularyId
    - requires at least one grammarRuleId

    Do not classify ordinary conjugation or inflection as VOCABULARY_AND_GRAMMAR merely because
    grammar is technically involved.

    For example:

    Anna {{blank-5}} den Zug. (sehen)

    If the intended practice is simply:
    sehen -> sieht

    classify it as VOCABULARY_FORM.

    BLANK DESIGN

    Every blank must:
    - have a globally unique blankToken such as {{blank-1}}
    - have that token occur exactly once
    - have exactly one intended correct answer
    - target meaningful German rather than punctuation or trivial filler
    - have enough context and/or cue information to make the answer unambiguous
    - preserve normal German capitalization, umlauts, articles, particles, and spacing
    - have a concise explanation of why the exact form is correct

    exactAnswer may contain one or multiple words.

    Examples:
    - sieht
    - den Zug
    - zu spät
    - steht auf
    - hat gesehen
    - mit dem Zug

    Include the complete expression required by natural German.

    For example, if the sentence requires:

    zu spät

    exactAnswer must be:
    zu spät

    not merely:
    spät

    If the complete missing noun phrase is:

    den Zug

    exactAnswer must be:
    den Zug

    unless "Zug" is already visible outside the blank.

    EXACT-ANSWER SAFETY

    The learner must eventually enter the exact answer for every blank.

    Therefore avoid blanks where several ordinary German answers would all be valid.

    Make sure:
    - tense is clear
    - subject/person is clear
    - required case is clear
    - intended lexical item is clear
    - the context rules out common alternative synonyms
    - the missing span has one natural expected form

    If several answers would reasonably work, rewrite the sentence or cue until the intended
    answer becomes unambiguous.

    NATURALNESS

    Do not create awkward sentences merely to consume vocabulary.

    Do not repeat the same idea merely to create additional blanks.

    Do not turn the paragraph into unrelated grammar-example sentences.

    After all answers are inserted, the paragraph should read like a small piece of normal,
    coherent German.

    Vocabulary sources:
    %s

    Grammar sources:
    %s
    """.formatted(
    context.learnerLevel(),
    context.vocabulary(),
    context.grammarRules());
}
public static String readingUsedVocabularySelection(
      List<ReadingPracticeVocabularySeed> vocabulary,
      String readingText) {
    String vocabList = formatVocabulary(vocabulary);

    return """
        You are given:
        1. a learner vocabulary list (German surface form + translation)
        2. a German reading text

        Goal:
        Return ONLY the vocabulary surface forms from the provided list that are actually used
        in the reading text.

        Matching Rules:
        - Match against the provided German vocabulary list only.
        - A vocabulary item counts as used if it appears in the reading text,
          including natural inflected forms such as plural nouns, declined adjectives,
          or conjugated verbs.
        - Return the exact `surface` values from the provided list.
        - Do NOT invent new values.
        - Do NOT return translations.
        - If none are used, return an empty list.

        Learner Vocabulary (German - translation):
        %s

        Reading text:
        %s
        """.formatted(vocabList, readingText);
  }

  private static String formatWritingVocabulary(List<WritingPracticeVocabularySeed> vocabulary) {
    StringBuilder builder = new StringBuilder();

    for (WritingPracticeVocabularySeed v : vocabulary) {
      builder.append(v.surface())
          .append(" - ")
          .append(v.translation())
          .append("\n");
    }

    return builder.toString();
  }

  public static String writingTopicSelection(
      List<WritingPracticeVocabularySeed> vocabulary,
      List<String> previousTopics,
      LanguageLevel difficultyLevel) {
    String vocabList = formatWritingVocabulary(vocabulary);
    String topics = previousTopics == null || previousTopics.isEmpty()
        ? "(none)"
        : String.join("\n", previousTopics);

    return """
                Act as an expert writing coach for a German learner.

        CEFR level: %s

        Goal:
        Pick exactly one fresh writing topic suitable for a short bilingual paragraph.

        Rules:
        - Return EXACTLY 1 topic.
        - The topic must be a SHORT to MEDIUM PHRASE (4–20 words).
        - Do NOT write a full sentence.
        - The topic must describe a real-life situation or experience.
        - The topic must allow a natural paragraph to be written.
        - Avoid repeating or closely paraphrasing recent topics.

        Important Restrictions:
        - Do NOT generate topics about learning vocabulary, word meanings, grammar, or language explanations.
        - Do NOT create meta-topics about words or language itself.

        Steps:
        1. Generate 5 candidate topics internally.
        2. Choose the most natural and useful topic.

        Recent topics to avoid:
        %s

        Learner vocabulary (German - translation):
        %s
                """.formatted(difficultyLevel, topics, vocabList);
  }

  public static String writingBilingualContent(
    String topic,
    List<WritingPracticeVocabularySeed> vocabulary,
    LanguageLevel difficultyLevel,
    List<String> grammarRuleTitles) {

  String vocabList = formatWritingVocabulary(vocabulary);

  String grammarTitles = grammarRuleTitles == null || grammarRuleTitles.isEmpty()
      ? "(none provided)"
      : grammarRuleTitles.stream()
          .filter(title -> title != null && !title.isBlank())
          .map(title -> "- " + title.trim())
          .collect(java.util.stream.Collectors.joining("\n"));

  return """
      You are an expert German language teacher creating a guided
      English-to-German writing exercise.

      CEFR Level: %s
      Topic: "%s"

      Primary Goal:
      Create one natural, coherent everyday situation that the learner can first
      read in English and then translate into German.

      The exercise should practice useful learner vocabulary and suitable grammar,
      but natural meaning and coherence are more important than vocabulary coverage.

      Priority Order:
      Follow this priority order strictly:

      1. A coherent and realistic situation.
      2. Natural English in the learner prompt.
      3. Natural and idiomatic German in the reference answer.
      4. Appropriate difficulty for the requested CEFR level.
      5. Useful practice of a suitable subset of learner vocabulary.
      6. Natural exposure to eligible grammar rules.

      Output Structure:
      Return:

      1. One English paragraph for the learner to translate into German.
      2. One correct German reference paragraph.
      3. `usedVocabulary`: the canonical surfaces of supplied learner vocabulary
         actually used in the German paragraph.

      The English and German paragraphs must express exactly the same meaning.

      Prefer the same number of sentences in both languages.
      Each English sentence should correspond clearly to one German sentence.
      Do not add, remove, or substantially rearrange information between languages.

      Situation and Coherence:
      - Build the paragraph around ONE clear situation, event, experience,
        decision, problem, conversation, or sequence of actions.
      - Every sentence should belong naturally to that same situation.
      - Each sentence should normally add new information or move the situation forward.
      - Do not add unrelated information merely to use additional vocabulary.
      - Do not repeat the same idea using slightly different wording.
      - When appropriate, give the paragraph a simple beginning,
        development, and conclusion.
      - The result must feel like a genuine mini-situation, not a vocabulary list
        disguised as a paragraph.

      English Paragraph Rules:
      - Write natural contemporary English.
      - The English must sound natural independently of the German reference.
      - Do NOT create awkward English by translating German structures literally.
      - Do NOT reverse-engineer unnatural English merely to force a German
        vocabulary item into the reference paragraph.
      - Prefer expressions a normal English speaker would actually use.
      - Keep the meaning reasonably direct so that translation into German
        remains appropriate for the requested CEFR level.
      - Avoid idioms, literary expressions, unnecessarily abstract ideas,
        and complicated English syntax.

      German Paragraph Rules:
      - Express exactly the same meaning as the English paragraph.
      - Use natural, idiomatic German.
      - Do not translate the English mechanically word-for-word when German
        requires a different natural structure.
      - Use grammar and sentence complexity appropriate to the requested CEFR level.
      - Prefer common everyday constructions.
      - Present tense, Perfekt, modal verbs, subordinate clauses, questions,
        connectors, and other structures may appear when appropriate to the level.

      Vocabulary Selection:
      - The learner vocabulary is a pool of possible target vocabulary,
        NOT a checklist.
      - First decide which vocabulary items genuinely fit the topic and situation.
      - Use only that coherent subset.
      - It is completely acceptable to leave many provided vocabulary items unused.
      - Never add an unrelated sentence merely because unused vocabulary remains.
      - Never sacrifice coherence or naturalness to increase vocabulary coverage.
      - Prefer vocabulary that naturally works together in the same semantic situation.
      - Combine several compatible learner vocabulary items in one sentence when natural.
      - Vocabulary may appear in any grammatically correct inflected form.

      Used Vocabulary Reporting:
      - Report only entries from the supplied learner vocabulary.
      - For every used item, return its original supplied German surface exactly.
      - If the German paragraph uses an inflected, declined, plural, conjugated,
        or separated form, still report the original supplied canonical surface.
      - Do not report the form appearing in the paragraph when it differs from
        the supplied canonical surface.
      - Do not report translations, synonyms, or invented surfaces.
      - Return an empty `usedVocabulary` list when no supplied item is used.

      Vocabulary Repetition:
      - Reuse a selected vocabulary item only when it naturally recurs in the situation.
      - One natural occurrence is sufficient.
      - A useful item may appear 2-3 times when repetition genuinely fits the text.
      - Do not repeat an idea simply to repeat a vocabulary item.
      - Do not force important vocabulary into multiple different sentences.
      - Natural contextual usage is more valuable than artificial repetition.

      Vocabulary Meaning:
      - Vocabulary translations may contain several possible meanings,
        senses, or notes separated by punctuation such as commas,
        semicolons, slashes, or parentheses.
      - Choose ONLY the meaning that naturally fits the current situation.
      - Do not attempt to represent every supplied English gloss.
      - If a vocabulary item's supplied meaning does not fit the topic naturally,
        leave that vocabulary item unused.
      - Prefer the common everyday meaning when several meanings are possible.

      Behavior to Avoid:
      - Do not add unrelated sentences merely to consume remaining target vocabulary.
      - Do not create unnatural English by translating a desired German structure literally.
      - Do not create multiple sentences with essentially the same meaning merely
        to repeat vocabulary.
      - Do not construct a paragraph by independently fitting vocabulary items
        into sentences and then joining those sentences together.
      - Do not prefer vocabulary coverage over a believable sequence of ideas.

      Additional Vocabulary:
      - Use learner vocabulary whenever it fits naturally.
      - Additional vocabulary is allowed when necessary for coherent and natural expression.
      - Additional words should be common, useful, and appropriate to the requested
        CEFR level.
      - Avoid unnecessary rare, literary, technical, or highly specialized vocabulary.

      Additional Vocabulary by Level:
      - A1:
        Keep additional content vocabulary tightly controlled.
        Normally introduce only about 3-5 additional content words in the full exercise.
        If the situation requires many unfamiliar words, simplify the situation instead.

      - A2:
        Common additional vocabulary may be used when needed for natural expression,
        but avoid introducing a large amount of new thematic vocabulary at once.

      - B1:
        Common CEFR-appropriate vocabulary may be used more freely when needed
        for coherent and natural expression.
        Still avoid unnecessary lexical complexity.

      Function words, articles, pronouns, prepositions, auxiliaries,
      and ordinary connectors do not count as additional content vocabulary.

      Grammar Usage:
      - Eligible grammar-rule titles are provided below.
      - Choose only rules that naturally fit the topic, vocabulary,
        and requested CEFR level.
      - You do NOT need to use every eligible grammar rule.
      - It is acceptable to use no eligible rule if none fits naturally.
      - A useful grammar structure may recur a few times when natural.
      - Never distort either paragraph merely to demonstrate a grammar rule.
      - Ordinary grammar appropriate to the requested CEFR level may also be used.

      Difficulty and Sentence Length:
      Adapt sentence complexity to the requested CEFR level.

      A1:
      - Usually 4-10 words per sentence.
      - Prefer very common vocabulary and straightforward main clauses.
      - Use more advanced structures mainly when they are explicitly eligible
        grammar rules.

      A2:
      - Usually 6-14 words per sentence.
      - Allow common connectors, modal verbs, Perfekt, subordinate clauses,
        and moderately varied word order.

      B1:
      - Usually 8-18 words per sentence.
      - Allow connected clauses, explanations, reasons, conditions,
        subordinate clauses, and more developed thoughts.

      These are guidelines, not hard limits.
      Occasional shorter or longer sentences are allowed when natural.

      Paragraph Length:
      - Usually 4-7 sentences.
      - Prefer a compact coherent exercise over a longer paragraph
        containing filler or unrelated ideas.

      Final Quality Check:
      Before returning the answer, internally verify:

      - Does the English sound natural on its own?
      - Does the German sound natural on its own?
      - Do both paragraphs express the same meaning?
      - Does every sentence belong to the same situation?
      - Does each sentence add meaningful information?
      - Did any sentence exist mainly to force vocabulary?
      - Did you use a sensible vocabulary subset rather than trying to cover the list?
      - Are ambiguous vocabulary items used only in a meaning appropriate
        to the context?
      - Is additional vocabulary appropriate to the requested CEFR level?
      - Is the grammar appropriate to the requested CEFR level?

      If any sentence fails these checks, revise it before returning the result.

      Style:
      - Clear, contemporary, everyday language.
      - No bullet points, numbering, headings, explanations, or teaching notes
        inside the generated exercise.
      - Output only the bilingual exercise required by the response schema.

      Learner Vocabulary (German - translation):
      %s

      Eligible Grammar-Rule Titles:
      %s
      """.formatted(
          difficultyLevel,
          topic,
          vocabList,
          grammarTitles);
}

  public static String writingSentencePairSplit(String englishParagraph, String germanParagraph) {
    return """
        You are given one English paragraph and one German paragraph that express the same meaning.

        Goal:
        Split them into aligned sentence pairs with 1-to-1 mapping.

        Rules:
        - Preserve wording exactly; do not rewrite.
        - Preserve order exactly.
        - Each English sentence must map to exactly one German sentence.
        - If sentence counts differ slightly, choose the closest faithful 1-to-1 segmentation.
        - Return only the aligned sentence pairs.

        English paragraph:
        %s

        German paragraph:
        %s
        """.formatted(englishParagraph, germanParagraph);
  }

  public static String writingSubmissionFeedback(
      String englishParagraph,
      String referenceGermanParagraph,
      String submittedGermanParagraph,
      java.util.List<com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem> grammarCatalog) {
    String grammarCatalogText = (grammarCatalog == null || grammarCatalog.isEmpty())
        ? "(none yet)"
        : grammarCatalog.stream()
            .map(item -> "- " + item.identifier() + " | " + item.name() + " | " + (item.hint() == null ? "" : item.hint()))
            .collect(java.util.stream.Collectors.joining("\n"));

    return """
        You are expert German writing coach.

        Goal:
        Evaluate submitted German translation against source English paragraph and reference German paragraph.
        Return JSON with feedback plus grammar issue mappings.

        Focus:
        - grammar mistakes
        - word choice mistakes
        - meaning mismatch
        - major spelling mistakes that change meaning

        Rules:
        - Keep feedback practical and specific.
        - Keep tone encouraging but direct.
        - Max 140 words.
        - Mention 3-6 important issues, not every tiny issue.
        - Include 1 short actionable next-step tip at end.
        - `feedback` must be plain text, no bullets, no markdown.
        - For each major grammar issue, map to an existing grammar identifier from catalog when possible.
        - If no good match exists, set `ruleIdentifier` to empty string and provide a concise `fallbackExplanation`.

        Return STRICT JSON object only:
        {
          "feedback": "string",
          "grammarIssues": [
            {
              "issueText": "short issue anchor from learner text",
              "message": "brief correction message",
              "suggestion": "suggested correction",
              "ruleIdentifier": "existing identifier or empty",
              "fallbackExplanation": "brief explanation used when identifier missing"
            }
          ]
        }

        Existing grammar identifiers catalog:
        %s

        English paragraph:
        %s

        Reference German paragraph:
        %s

        Submitted German paragraph:
        %s
        """.formatted(grammarCatalogText, englishParagraph, referenceGermanParagraph, submittedGermanParagraph);
  }

  public static String writingMeaningAnalyzer(String learnerLevel,
                                              String englishPrompt,
                                              String referenceGermanParagraph,
                                              String learnerGermanAnswer) {
    return """
        You are the Meaning Analyzer for a German writing practice system.

        Your job is to analyze whether the learner communicated the intended meaning of the English prompt.

        You will receive:
        - learner writing level,
        - English prompt,
        - reference/model German paragraph,
        - learner German answer.

        Analyze meaning only. Do not evaluate target vocabulary in detail. Do not teach grammar. Do not produce learner-facing feedback.

        Compare the learner's answer with the English prompt and use the reference German paragraph as support.

        Determine:
        1. Which prompt ideas were communicated.
        2. Which prompt ideas were missed.
        3. Which prompt ideas were distorted or changed.
        4. Whether the overall meaning coverage is good, partial, weak, or not enough evidence.
        5. Which learner sentences/phrases correspond to which prompt ideas, when possible.

        Important behavior:
        - Judge meaning separately from grammar perfection.
        - Broken German can still communicate partial meaning.
        - English words inside the learner answer may still carry meaning, but should not be treated as successful German vocabulary use.
        - Do not correct all grammar.
        - Do not produce teaching feedback.
        - Do not generate motivational filler.
        - Return only the structured output expected by the framework.

        Learner writing level: %s

        English prompt:
        %s

        Reference/model German paragraph:
        %s

        Learner German answer:
        %s
        """.formatted(learnerLevel, englishPrompt, referenceGermanParagraph, learnerGermanAnswer);
  }

  public static String writingVocabularyEvaluator(String learnerLevel,
                                                  String englishPrompt,
                                                  String referenceGermanParagraph,
                                                  String learnerGermanAnswer,
                                                  java.util.List<com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackVocabularyItem> selectedVocabulary,
                                                  Object meaningAnalysis) {
    return """
        You are the Vocabulary Evaluator for a German writing practice system.

        Your job is to evaluate the learner's use of the selected target vocabulary in their German writing answer.

        You will receive:
        - learner writing level,
        - English prompt,
        - reference/model German paragraph,
        - learner German answer,
        - selected target vocabulary,
        - meaning analysis result.

        Evaluate only the selected target vocabulary. Do not give general grammar feedback. Do not produce learner-facing feedback.

        For each target vocabulary item, decide:
        1. Was the item expected or useful for this task?
        2. Did the learner attempt it?
        3. Did the learner use the German target word/chunk?
        4. Did the learner use an English placeholder instead?
        5. Was the German word recalled correctly?
        6. Was the form, article, preposition, separable form, or phrase structure acceptable?
        7. Was the usage natural enough for the learner's level?
        8. What production-memory signal should this item receive?

        Important behavior:
        - If the learner recalled the correct German word but used the wrong form, article, preposition, word order, or tense, treat it as partially correct rather than fully wrong.
        - If the learner used English instead of German, mark that clearly.
        - If the vocabulary item was optional and the answer gives no evidence, do not force a failure.
        - Use the meaning analysis to distinguish missed ideas from grammar/form mistakes.
        - Do not punish the same issue twice when possible.
        - Do not teach grammar.
        - Do not generate final feedback.
        - Return only the structured output expected by the framework.

        Learner writing level: %s

        English prompt:
        %s

        Reference/model German paragraph:
        %s

        Learner German answer:
        %s

        Selected target vocabulary:
        %s

        Meaning analysis result:
        %s
        """.formatted(learnerLevel, englishPrompt, referenceGermanParagraph, learnerGermanAnswer, selectedVocabulary, meaningAnalysis);
  }

  public static String writingGrammarIssueDetector(String learnerLevel,
                                                   String englishPrompt,
                                                   String referenceGermanParagraph,
                                                   String learnerGermanAnswer,
                                                   java.util.List<com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem> grammarCatalog,
                                                   Object meaningAnalysis,
                                                   Object vocabularyEvaluation) {
    return """
        You are the Grammar/Writing Issue Detector for a German writing practice system.

        Your job is to detect and rank the learner's grammar and writing issues.

        You will receive:
        - learner writing level,
        - English prompt,
        - reference/model German paragraph,
        - learner German answer,
        - allowed/current grammar rules,
        - meaning analysis result,
        - vocabulary evaluation result.

        Detect grammar and writing issues using the provided grammar rules as the main reference. You may also detect obvious beginner-level issues if they seriously affect meaning.

        Do not evaluate target vocabulary again except when a grammar issue affects a vocabulary item.
        Do not update memory.
        Do not produce final learner-facing feedback.
        Do not produce a long grammar lesson.

        For each important issue, identify:
        1. The grammar rule or writing issue involved.
        2. The learner's problematic text.
        3. The corrected text.
        4. A short practical explanation.
        5. The priority of the issue.
        6. Whether the issue should be a top feedback candidate.

        Issue priority should prefer:
        - repeated mistakes,
        - mistakes that block meaning,
        - mistakes connected to selected vocabulary,
        - issues from the learner's current grammar rules,
        - high-frequency beginner issues,
        - issues that can produce useful micro-practice.

        Important behavior:
        - Detect many issues if needed, but mark only the most useful ones as top candidates.
        - Normally final feedback will show only the top 3 issues.
        - Keep explanations short.
        - Do not explain full grammar systems.
        - Do not teach unrelated grammar.
        - Return only the structured output expected by the framework.

        Learner writing level: %s

        English prompt:
        %s

        Reference/model German paragraph:
        %s

        Learner German answer:
        %s

        Allowed/current grammar rules:
        %s

        Meaning analysis result:
        %s

        Vocabulary evaluation result:
        %s
        """.formatted(learnerLevel, englishPrompt, referenceGermanParagraph, learnerGermanAnswer, grammarCatalog, meaningAnalysis, vocabularyEvaluation);
  }

  public static String writingFeedbackComposer(String learnerLevel,
                                               String englishPrompt,
                                               String referenceGermanParagraph,
                                               String learnerGermanAnswer,
                                               Object meaningAnalysis,
                                               Object vocabularyEvaluation,
                                               Object grammarIssues,
                                               java.util.List<com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult.Issue> selectedTopIssues) {
    return """
        You are the Feedback Composer for a German language-learning platform.

        You will receive structured results from:
        - meaning analysis,
        - target vocabulary evaluation,
        - grammar/writing issue detection.

        Your job is to turn those results into clear, concise, learner-facing feedback.

        Do not redo the full analysis unless the provided analysis is obviously inconsistent.
        Do not produce a long grammar lecture.
        Do not explain full grammar systems.
        Do not overwhelm the learner.

        Create feedback with these goals:
        1. Show whether the intended meaning was communicated.
        2. Show a corrected German paragraph.
        3. Explain only the most important issues, normally maximum 3.
        4. Show target vocabulary performance in a compact way.
        5. Give a few useful sentence corrections.
        6. Give 2 to 4 micro-practice items.
        7. End with a clear next focus.

        Style requirements:
        - Use simple explanations appropriate to the learner's writing level.
        - Be strict but encouraging.
        - Prefer practical corrections over theory.
        - Keep grammar explanations short.
        - If the learner used English words inside German, point this out clearly.
        - If the learner remembered the right German word but used the wrong form, treat that as partial success.
        - Do not include unrelated grammar.
        - Do not include a giant feedback paragraph.
        - Use headings or clear sections.
        - Return only the structured output expected by the framework.

        Learner writing level: %s

        English prompt:
        %s

        Reference/model German paragraph:
        %s

        Learner German answer:
        %s

        Meaning analysis:
        %s

        Vocabulary evaluation:
        %s

        Grammar/writing issue detection:
        %s

        Selected final top issues:
        %s
        """.formatted(learnerLevel, englishPrompt, referenceGermanParagraph, learnerGermanAnswer, meaningAnalysis, vocabularyEvaluation, grammarIssues, selectedTopIssues);
  }


  public static String readingContentParagraphSentenceSplit(List<String> paragraphs) {
    var builder = new StringBuilder();
    for (int i = 0; i < paragraphs.size(); i++) {
      builder.append("Paragraph ").append(i).append(":\n")
          .append(paragraphs.get(i)).append("\n\n");
    }

    return """
                You are given German reading paragraphs.

        Goal:
        Split each paragraph into its original sentences without rewriting or reordering.

        Rules:
        - Do not change any words, punctuation, or casing.
        - Preserve sentence order exactly as in the paragraph.
        - Use the provided paragraph index as `paragraphIndex`.

        Paragraphs:
        %s
                """.formatted(builder.toString());
  }

  public static String sentenceGeneratorNew(
      LangConfigsAdaptive config,
      List<Sentence.SentenceData> previousSentences) {

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
        config.quantity().sentenceCount());
  }

  private static String formatVocabularyClozeSeeds(List<VocabularyClozeGenerationSeed> vocabulary) {
    StringBuilder builder = new StringBuilder();

    for (VocabularyClozeGenerationSeed v : vocabulary) {
      builder.append(v.surface())
          .append(" | ")
          .append(v.translation())
          .append("\n");
    }

    return builder.toString();
  }

  public static String vocabularyClozeSentences(
    String topic,
    List<VocabularyClozeGenerationSeed> vocabulary) {

  String vocabList = formatVocabularyClozeSeeds(vocabulary);

  return """
You are a German teacher creating sentence-cloze vocabulary exercise for A2.

Topic context: "%s"

Task
For each vocabulary entry, create exactly ONE German sentence containing a blank.

Vocabulary concept
Each vocabulary entry represents a lemma (base dictionary form).
The blank should normally require a natural surface form derived from that lemma.

Prefer:
- conjugated verb forms
- plural or case forms of nouns
- separable verb constructions
- adjective agreement forms

Avoid:
- using the base lemma when a natural sentence would use an inflected form
- sentences where the blank appears only as an infinitive unless grammar requires it

Cloze rules
- The sentence must contain blanks representing the missing vocabulary.
- Each blank must be written as "____".
- If the vocabulary entry contains multiple words (e.g., "um zu", "immer noch"),
the sentence must contain the SAME number of blanks as words.
- Example:
  vocabulary: "immer noch" → cloze must contain "____ ____"

- Do not merge multiple-word vocabulary into a single blank.
- The blanks must appear exactly where the vocabulary phrase would appear.
- You may use multiple learner vocabulary words in the same sentence.
- Prefer reusing learner vocabulary instead of introducing new thematic words.
- Only one vocabulary entry may be blanked and tested in each sentence.
- The number of blanks must equal the number of words in answerWords.

Hint rules
- The hint MUST be in English only.
- The hint MUST NOT be in German under any circumstance.
- If the vocabulary meaning is in German, you MUST translate it into natural English.
- The hint must represent the meaning of the word as used in the sentence.

- The hint MUST be the most common, simplest, and most frequently used everyday meaning.
- Prefer the shortest and most basic translation a beginner would learn first.
- Avoid formal, rare, indirect, or descriptive meanings.
- Avoid paraphrases such as "be familiar with" if "know" is possible.
- Avoid phrases such as "perform" if "play" is more natural.
- Avoid multi-word explanations when a single common word exists.

- Do NOT copy the German lemma or phrase into the hint.
- The hint must be very short (1–3 words).

- If the vocabulary input provides a meaning:
  → use that meaning ONLY IF it is simple and common.
  → otherwise simplify it to the most common equivalent.

- If multiple meanings exist:
  → ALWAYS choose the most frequent and typical everyday meaning.


Consistency Rule
- The hint and the answer_text must align semantically.
- A learner should be able to reconstruct the correct answer using ONLY the hint and sentence context.
- If the hint would lead to a different valid word, it is INVALID.
- The hint must lead to the MOST LIKELY answer a beginner would give.
- If the hint could lead to a more common alternative word than the target answer, it is INVALID.

German Sentence constraints
- Natural everyday German.
- Sentences must be clear and easy to understand.
- Word limit is 6–14 words per sentence.

Sentence Complexity Constraints
- Try to add those sentences where the meaning of the word can be inferred from the sentence.
This is a soft constraint and must never disturb natural German.
- Use everyday, common and easy words when building a sentence.
- Sentences must be clear and easy to translate.
- Combine multiple learner vocabulary words in the same sentence when natural.
- Do not force vocabulary if it makes the sentence unnatural.
- Prefer sentences that stay within the topic context when possible.
- Prefer sentences where surrounding words give clear contextual clues for the missing word.
- When natural, include small contextual details (e.g. time, place, situation) that help infer the meaning.

Vocabulary Control
- Prefer using the provided learner vocabulary whenever possible.
- Avoid introducing many new thematic words.
- If additional words are necessary, prefer very common everyday German.

Grammar variation
Use a wide range of natural German grammar when forming the sentence.

Prefer to vary grammatical structures across sentences, including but not limited to:

Verb forms
- present tense (Präsens)
- present perfect (Perfekt)
- simple past (Präteritum) when natural
- future constructions with "werden"
- imperative when natural

Verb constructions
- modal verb + infinitive (kann, muss, soll, will, darf)
- separable verbs in split form
- reflexive verbs
- verb + zu constructions
- um ... zu purpose clauses

Clause structures
- subordinate clauses with "weil", "wenn", "dass", "obwohl" (but not limited to)
- relative clauses
- simple coordinating clauses (und, aber, oder and others)

Noun grammar
- plural forms
- case variation (Akkusativ, Dativ, Genitiv where natural)

Adjective grammar
- adjective agreement
- comparative and superlative forms

Important
Do not force grammar variation artificially.
Use the structure that sounds most natural for the sentence.

Validation rule
- Before finalizing, check:
  Does the hint uniquely lead to the correct answer in this sentence?
  If not, regenerate.
  - Check: Is this the simplest and most common translation a beginner would learn first?
  If not, regenerate.

Learner vocabulary (German | translation):
%s
  """.formatted(topic, vocabList);
	}

  public static String studyAnswerEvaluation(String sentenceWithBlank,
                                             String expectedAnswer,
                                             String answerTranslation,
                                             String hint,
                                             String userAnswer,
                                             java.util.List<com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem> grammarCatalog) {
    String grammarCatalogText = (grammarCatalog == null || grammarCatalog.isEmpty())
        ? "(none yet)"
        : grammarCatalog.stream()
            .map(item -> "- " + item.identifier() + " | " + item.name() + " | " + (item.hint() == null ? "" : item.hint()))
            .collect(java.util.stream.Collectors.joining("\n"));

    return """
Evaluate a learner answer for a German fill-in-the-blank exercise.

Return STRICT JSON object only with this schema:
{
  "semanticMatch": number,
  "formAccuracy": number,
  "confidence": number,
  "feedback": string,
  "grammarIssues": [
    {
      "issueText": "short issue anchor",
      "message": "brief correction message",
      "suggestion": "suggested correction",
      "ruleIdentifier": "existing identifier or empty",
      "fallbackExplanation": "brief explanation"
    }
  ]
}

Rules:
- semanticMatch: 0.0..1.0 meaning correctness vs expected answer.
- formAccuracy: 0.0..1.0 grammatical/spelling/form correctness for expected answer.
- confidence: 0.0..1.0 certainty.
- feedback: max 25 words, direct, useful.

Scoring guidance:
- Prioritize meaning first.
- If meaning is correct but there are form-only mistakes (article, capitalization, inflection, minor spelling),
  keep semanticMatch high and lower formAccuracy.
- Use low semanticMatch only when meaning/lexeme is wrong.
- Do not treat capitalization-only mistakes as semantic errors.
- Reuse catalog identifier whenever matching grammar rule already exists.
- If no match exists, keep ruleIdentifier empty and provide fallbackExplanation.
- Typical targets:
  - correct meaning + small form error: semanticMatch >= 0.70, formAccuracy in 0.45..0.80
  - wrong meaning/word: semanticMatch < 0.45

Exercise:
Sentence with blank: %s
Expected answer: %s
Expected translation: %s
Hint: %s
User answer: %s
Existing grammar identifiers catalog:
%s
""".formatted(
      sentenceWithBlank,
      expectedAnswer,
      answerTranslation == null ? "" : answerTranslation,
      hint == null ? "" : hint,
      userAnswer,
      grammarCatalogText
    );
  }

  public static String grammarRuleDrafts(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
    String existingCatalog = (existingRules == null || existingRules.isEmpty())
        ? "(none)"
        : existingRules.stream()
            .map(rule -> "identifier=%s | name=%s | level=%s".formatted(rule.identifier(), rule.name(), rule.level()))
            .reduce((a, b) -> a + "\n" + b)
            .orElse("(none)");

    return """
You are designing a German grammar curriculum catalog.

Return STRICT JSON array only. Each item:
{
  "identifier": "kebab-case unique id",
  "name": "short generic grammar rule name",
  "level": "A1|A2|B1|B2|C1|C2",
  "targetLanguage": "language code"
}

Rules:
- Return exactly %d items.
- Rules must be generic and reusable, not word-specific.
- Keep identifiers descriptive and stable.
- Do not include duplicates.
- Do not return rules that overlap with existing catalog identifiers or rule meanings.
- Check uniqueness against the full existing catalog across all levels.
- Propose rules appropriate for requested level only.

Requested level: %s
Target language: %s
Existing catalog (all levels):
%s
""".formatted(count, level, targetLanguage, existingCatalog);
  }

  public static String grammarRuleDetails(String identifier, String name, String level, String targetLanguage) {
    return """
You are writing grammar learning content for one rule.

Return STRICT JSON object only:
{
  "identifier": "same identifier",
  "name": "same rule name",
  "level": "same CEFR level",
  "targetLanguage": "same language code",
  "explanationParagraphs": ["paragraph 1", "paragraph 2"],
  "explanationExamples": [
    {"sentence":"...", "translation":"...", "note":"..."}
  ]
}

Rules:
- Keep explanations concise and generic.
- Write explanationParagraphs in clear English.
- Provide 2-4 explanation paragraphs.
- Provide 3-6 examples.
- Examples must be natural German and match the rule.

Identifier: %s
Rule name: %s
Level: %s
Target language: %s
""".formatted(identifier, name, level, targetLanguage);
  }

  public static String grammarLevelReassignment(List<GrammarLevelReassignmentInput> grammarRules) {
    String rules = (grammarRules == null || grammarRules.isEmpty())
        ? "(none)"
        : grammarRules.stream()
            .map(rule -> """
                ID: %s
                Title: %s
                Current difficulty level: %s
                Explanation/content:
                %s
                Examples:
                %s
                """.formatted(
                rule.id(),
                rule.title(),
                rule.currentLevel(),
                String.join("\n", rule.explanationParagraphs() == null ? List.of() : rule.explanationParagraphs()),
                formatGrammarExamples(rule.examples())
            ))
            .reduce((a, b) -> a + "\n---\n" + b)
            .orElse("(none)");

    return """
You are reviewing existing German grammar rules for a language-learning platform.

Your task is to verify whether each grammar rule is assigned to the correct CEFR difficulty level and propose a corrected level only when necessary.

You will receive a collection of grammar rules. Each rule may contain:
- its stable ID,
- title,
- current difficulty level,
- explanation or rule content,
- examples.

Review every rule independently.

For each grammar rule:

1. Determine the CEFR level at which the grammar concept should normally be introduced or actively practiced.
2. Compare that level with the rule's current assigned level.
3. Keep the current level when it is already appropriate.
4. Propose a different level only when the current assignment is meaningfully incorrect.
5. Provide a short reason for the decision.

Classification rules:

- Judge the complexity of the grammar concept itself.
- Do not raise the level merely because an example contains difficult vocabulary.
- Do not lower the level merely because the explanation is written simply.
- Classify according to the knowledge required to understand and use the rule.
- Prefer the earliest realistic CEFR level at which the learner can actively practise the rule.
- Use only difficulty levels supported by the application: A1, A2, B1, B2, C1.
- Preserve every grammar-rule ID exactly.
- Process every supplied rule once.
- Do not rewrite, merge, split, delete, or generate grammar rules.
- Return only the structured output expected by the framework.

Grammar rules:
%s
""".formatted(rules);
  }

  private static String formatGrammarExamples(List<GrammarLevelReassignmentInput.GrammarExample> examples) {
    if (examples == null || examples.isEmpty()) {
      return "(none)";
    }
    return examples.stream()
        .map(example -> "- %s -> %s".formatted(example.sentence(), example.translation()))
        .reduce((a, b) -> a + "\n" + b)
        .orElse("(none)");
  }

}
