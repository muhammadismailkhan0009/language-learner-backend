package com.myriadcode.languagelearner.language_content.domain.model.language_settings.german;

public class GermanAdaptive {

    // ============================================================
    // CEFR TAG
    // ============================================================

    public enum CEFR {A1, A2, B1, B2, C1, C2}

    // ============================================================
    // 1. COMMUNICATIVE FUNCTIONS
    // ============================================================

    public interface CommunicativeFunction {
    }

    public enum CommunicativeFunctionGroup {
        BASIC_INTERACTION,
        SOCIAL_COMMUNICATION,
        DAILY_LIFE_FUNCTIONS,
        PROFESSIONAL_ACADEMIC_FUNCTIONS,
    }

    public enum CommunicativeFunctionEnum implements CommunicativeFunction {

        GREET(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        INTRODUCE_SELF(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        INTRODUCE_OTHERS(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        ASK_AND_ANSWER_SIMPLE_QUESTIONS(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        DESCRIBE_OBJECTS_AND_CLARIFY(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        DESCRIBE_PEOPLE_AND_CLARIFY(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        TALK_ABOUT_AND_ASK_ABOUT_DAY(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        TALK_ABOUT_AND_ASK_ABOUT_ROUTINE(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A1),
        MAKE_REQUESTS_AND_RESPOND(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A2),
        ASK_FOR_AND_GIVE_CLARIFICATION(CommunicativeFunctionGroup.BASIC_INTERACTION, CEFR.A2),

        EXPRESS_AND_DISCUSS_OPINIONS(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.A2),
        AGREE_AND_DISAGREE(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.A2),
        TALK_ABOUT_AND_ASK_ABOUT_PREFERENCES(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.A2),
        GIVE_AND_RECEIVE_COMPLIMENTS(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.A2),
        MAKE_AND_RESPOND_TO_INVITATIONS(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.A2),
        COMPARE_AND_DISCUSS_COMPARISONS(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.B1),
        NARRATE_EVENTS_AND_ASK_FOLLOWUPS(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.B1),
        EXPRESS_FEELINGS_AND_ASK_ABOUT_FEELINGS(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.B1),
        TALK_ABOUT_FUTURE_PLANS(CommunicativeFunctionGroup.SOCIAL_COMMUNICATION, CEFR.B1),

        ASK_FOR_AND_GIVE_DIRECTIONS(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.A1),
        ASK_FOR_AND_GIVE_HELP(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.A2),
        APOLOGIZE_AND_ACCEPT_APOLOGIES(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.A1),
        MAKE_AND_CONFIRM_APPOINTMENTS(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.A2),
        CONFIRM_AND_CHECK_INFORMATION(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.A2),
        DESCRIBE_AND_CLARIFY_PROCESSES(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.B1),
        GIVE_AND_FOLLOW_INSTRUCTIONS(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.A2),
        EXPLAIN_AND_DISCUSS_PROBLEMS(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.B1),
        COMPLAIN_AND_HANDLE_RESPONSES(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.B1),
        NEGOTIATE_AND_REACH_AGREEMENT(CommunicativeFunctionGroup.DAILY_LIFE_FUNCTIONS, CEFR.B2),

        REPORT_BLOCKERS_AND_CLARIFY(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.B1),
        PARTICIPATE_IN_MEETINGS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.B2),
        SUMMARIZE_AND_REQUEST_SUMMARIES(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.B2),
        WRITE_AND_PROCESS_FORMAL_EMAILS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.B2),
        PRESENT_IDEAS_AND_HANDLE_QUESTIONS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.B2),
        EXPLAIN_AND_REQUEST_TECHNICAL_CONCEPTS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.C1),
        DISCUSS_AND_CHALLENGE_ABSTRACT_TOPICS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.C1),
        SUPPORT_AND_EVALUATE_ARGUMENTS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.C1),
        HANDLE_FORMAL_DISAGREEMENTS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.C1),
        REPORT_SPEECH_AND_INTERPRET_REPORTS(CommunicativeFunctionGroup.PROFESSIONAL_ACADEMIC_FUNCTIONS, CEFR.B2);


        private final CEFR level;
        private final CommunicativeFunctionGroup group;

        CommunicativeFunctionEnum(CommunicativeFunctionGroup group, CEFR level) {
            this.level = level;
            this.group = group;
        }

        public CEFR level() {
            return level;
        }

        public CommunicativeFunctionGroup group() {
            return group;
        }
    }
    // ============================================================
    // BASIC INTERACTION FUNCTIONS (A1–A2)
    // ============================================================

    public interface GrammarRule {
    }

    public enum GrammarRuleGroup {
        FUNCTIONAL_GRAMMAR,
        SOCIAL_PROFESSIONAL_GRAMMAR,
        ADVANCED_GRAMMAR
    }

    public enum GrammarRuleEnum implements GrammarRule {
        MAIN_CLAUSE_WORD_ORDER(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        PRESENT_TENSE(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        YES_NO_QUESTIONS(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        WH_QUESTIONS(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        NEGATION(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        NOUN_ADJECTIVE_BASICS(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        PRONOUNS_BASIC(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        POSSESSIVES_BASIC(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        BASIC_PREPOSITIONS(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        MODAL_VERBS_BASIC(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        SEPARABLE_VERBS(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A1),
        IMPERATIVES(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A2),
        BASIC_COORDINATION(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A2),
        SIMPLE_TIME_EXPRESSIONS(GrammarRuleGroup.FUNCTIONAL_GRAMMAR, CEFR.A2),

        SUBORDINATE_CLAUSES(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        RELATIVE_CLAUSES(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        INDIRECT_QUESTIONS(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        PASSIVE_BASIC(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        ADJECTIVE_DECLENSION(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        ADVANCED_PREPOSITIONS(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        WORD_ORDER_COMPLEX(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B2),
        PAST_TENSES(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        FUTURE_FORMS(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B2),
        MODAL_VERBS_PAST(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),
        VERB_PREPOSITION_PATTERNS(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B2),
        COHESIVE_DEVICES(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B2),
        SIMPLE_CONDITIONALS(GrammarRuleGroup.SOCIAL_PROFESSIONAL_GRAMMAR, CEFR.B1),

        SUBJUNCTIVE_I(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C1),
        SUBJUNCTIVE_II(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C1),
        PASSIVE_COMPLEX(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C1),
        NOMINALIZATION(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C1),
        PARTICIPIAL_CONSTRUCTIONS(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C1),
        REDUCED_RELATIVE_CLAUSES(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C1),
        EMPHATIC_STRUCTURES(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C2),
        TOPICALIZATION(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C2),
        ADVANCED_CONNECTORS(GrammarRuleGroup.ADVANCED_GRAMMAR, CEFR.C1);
        private final CEFR level;
        private final GrammarRuleGroup group;

        GrammarRuleEnum(GrammarRuleGroup group, CEFR level) {
            this.level = level;
            this.group = group;
        }

        public CEFR level() {
            return level;
        }

        public GrammarRuleGroup group() {
            return group;
        }
    }

    // ============================================================
    // 3. SCENARIOS
    // ============================================================

    public interface Scenario {
    }

    public enum ScenarioGroup {
        DAILY_LIFE,
        LIVING_IN_GERMANY,
        UNIVERSITY_SCENARIO,
        WORK_LIFE_SCENARIO,
        SOCIAL_CULTURE_SCENARIO,

    }

    public enum ScenarioEnum implements Scenario {
        SELF_INTRODUCTION(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        GROCERY_SHOPPING(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        BUYING_CLOTHES(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        RESTAURANT(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        DIRECTIONS_AND_LOCATIONS(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        TRAIN_STATION(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        PUBLIC_TRANSPORT(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        SMALL_TALK_WEATHER(ScenarioGroup.DAILY_LIFE, CEFR.A1),
        MAKING_APPOINTMENTS(ScenarioGroup.DAILY_LIFE, CEFR.A2),
        CUSTOMER_SERVICE_CALL(ScenarioGroup.DAILY_LIFE, CEFR.B1),

        ANMELDUNG_BUERGERAMT(ScenarioGroup.LIVING_IN_GERMANY, CEFR.A2),
        BANK_ACCOUNT_OPENING(ScenarioGroup.LIVING_IN_GERMANY, CEFR.A2),
        BLOCKED_ACCOUNT_ISSUES(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B1),
        INTERNET_SIM_CONTRACT(ScenarioGroup.LIVING_IN_GERMANY, CEFR.A2),
        APARTMENT_VIEWING(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B1),
        RENT_NEGOTIATION(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B2),
        RENTAL_CONTRACT(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B2),
        LANDLORD_COMMUNICATION(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B1),
        UTILITY_BILLS(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B1),
        OFFICIAL_LETTERS(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B2),

        ENROLLMENT_IMMATRIKULATION(ScenarioGroup.LIVING_IN_GERMANY, CEFR.A2),
        SEMESTER_CONTRIBUTION(ScenarioGroup.LIVING_IN_GERMANY, CEFR.A2),
        LECTURES_PROFESSORS(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B1),
        GROUP_PROJECTS(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B1),
        DEADLINE_EXTENSION_REQUEST(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B2),
        TALKING_WITH_CLASSMATES(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B1),
        EXAM_PREPARATION(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B2),
        LIBRARY_USAGE(ScenarioGroup.LIVING_IN_GERMANY, CEFR.A2),
        CAMPUS_LIFE(ScenarioGroup.LIVING_IN_GERMANY, CEFR.A2),
        PROFESSOR_EMAILS(ScenarioGroup.LIVING_IN_GERMANY, CEFR.B2),

        DAILY_STANDUP(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B1),
        REPORTING_BLOCKERS(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B1),
        STATUS_UPDATES(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B2),
        MEETINGS_PARTICIPATION(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B2),
        PROFESSIONAL_EMAILS(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B2),
        NEGOTIATION_WORK(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B2),
        PROBLEM_SOLVING(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B2),
        TECHNICAL_EXPLANATIONS(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.C1),
        WORK_FEEDBACK(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.B2),
        WORK_CONFLICT_RESOLUTION(ScenarioGroup.WORK_LIFE_SCENARIO, CEFR.C1),

        MAKING_FRIENDS(ScenarioGroup.SOCIAL_CULTURE_SCENARIO, CEFR.A2),
        WEEKEND_PLANNING(ScenarioGroup.SOCIAL_CULTURE_SCENARIO, CEFR.A2),
        HOBBIES(ScenarioGroup.SOCIAL_CULTURE_SCENARIO, CEFR.A2),
        MOVIES_BOOKS(ScenarioGroup.SOCIAL_CULTURE_SCENARIO, CEFR.B1),
        NEWS_DISCUSSIONS(ScenarioGroup.SOCIAL_CULTURE_SCENARIO, CEFR.B2);

        private final CEFR level;
        private final ScenarioGroup group;

        ScenarioEnum(ScenarioGroup group, CEFR level) {
            this.level = level;
            this.group = group;
        }

        public CEFR level() {
            return level;
        }

        public ScenarioGroup group() {
            return group;
        }
    }


    // ============================================================
    // 4. EXERCISES
    // ============================================================

    public enum ExerciseDifficultyGroup {
        BEGINNER(BeginnerExercise.class),
        INTERMEDIATE(IntermediateExercise.class),
        ADVANCED(AdvancedExercise.class);

        private final Class<? extends Enum<?>> subgroup;

        ExerciseDifficultyGroup(Class<? extends Enum<?>> subgroup) {
            this.subgroup = subgroup;
        }
    }

    public enum BeginnerExercise {
        GAP_FILL(CEFR.A1),
        CLOZE_WORD(CEFR.A1),
        CLOZE_CHUNK(CEFR.A1),
        CLOZE_ARTICLE(CEFR.A1),
        TRANSLATION_L1_TO_L2_SIMPLE(CEFR.A1),
        TRANSLATION_L2_TO_L1_SIMPLE(CEFR.A1),
        REORDER_SIMPLE_SENTENCE(CEFR.A2),
        PICK_CORRECT_WORD(CEFR.A1),
        YES_NO_GRAMMAR_CHECK(CEFR.A1);

        private final CEFR level;

        BeginnerExercise(CEFR level) {
            this.level = level;
        }

        public CEFR level() {
            return level;
        }
    }

    public enum IntermediateExercise {
        CLOZE_GRAMMAR(CEFR.B1),
        TRANSLATION_L1_TO_L2(CEFR.B1),
        TRANSLATION_L2_TO_L1(CEFR.B1),
        TRANSFORM_NEGATIVE(CEFR.B1),
        TRANSFORM_QUESTION(CEFR.B1),
        TRANSFORM_TENSE(CEFR.B1),
        CHUNK_ASSEMBLY(CEFR.B1),
        MULTI_SENTENCE_GAP_FILL(CEFR.B1),
        SHORT_DIALOG_COMPLETION(CEFR.B1);

        private final CEFR level;

        IntermediateExercise(CEFR level) {
            this.level = level;
        }

        public CEFR level() {
            return level;
        }
    }

    public enum AdvancedExercise {
        PARAGRAPH_TRANSLATION_L1_TO_L2(CEFR.B2),
        PARAGRAPH_TRANSLATION_L2_TO_L1(CEFR.B2),
        SCENARIO_REWRITING(CEFR.C1),
        ROLEPLAY_RESPONSE(CEFR.C1),
        COMPLEX_SENTENCE_REORDER(CEFR.C1),
        MULTI_CLAUSE_TRANSFORMATION(CEFR.C1),
        SUMMARIZATION(CEFR.B2),
        ARGUMENT_RESTRUCTURING(CEFR.C1),
        REWRITE_WITH_GRAMMAR_CONSTRAINTS(CEFR.C2);

        private final CEFR level;

        AdvancedExercise(CEFR level) {
            this.level = level;
        }

        public CEFR level() {
            return level;
        }
    }
}
