package com.myriadcode.languagelearner.language_content.domain.model.language_settings.german;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.ContentGenerationQuantity;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive.CommunicativeFunctionEnum.*;
import static com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive.GrammarRuleEnum.*;
import static com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive.ScenarioEnum.*;

public enum GermanBlitz {

    // =========================
    // SELF INTRODUCTION (1–6)
    // =========================
    L1(SELF_INTRODUCTION, GREET, PRESENT_TENSE),
    L2(SELF_INTRODUCTION, INTRODUCE_SELF, MAIN_CLAUSE_WORD_ORDER),
    L3(SELF_INTRODUCTION, ASK_AND_ANSWER_SIMPLE_QUESTIONS, WH_QUESTIONS),
    L4(SELF_INTRODUCTION, DESCRIBE_PEOPLE_AND_CLARIFY, NOUN_ADJECTIVE_BASICS),
    L5(SELF_INTRODUCTION, TALK_ABOUT_AND_ASK_ABOUT_DAY, SIMPLE_TIME_EXPRESSIONS),
    L6(SELF_INTRODUCTION, MAKE_REQUESTS_AND_RESPOND, MODAL_VERBS_BASIC),

    // =========================
    // GROCERY SHOPPING (7–12)
    // =========================
    L7(GROCERY_SHOPPING, ASK_AND_ANSWER_SIMPLE_QUESTIONS, YES_NO_QUESTIONS),
    L8(GROCERY_SHOPPING, MAKE_REQUESTS_AND_RESPOND, SEPARABLE_VERBS),
    L9(GROCERY_SHOPPING, DESCRIBE_OBJECTS_AND_CLARIFY, BASIC_PREPOSITIONS),
    L10(GROCERY_SHOPPING, CONFIRM_AND_CHECK_INFORMATION, NEGATION),
    L11(GROCERY_SHOPPING, EXPRESS_AND_DISCUSS_OPINIONS, ADJECTIVE_DECLENSION),
    L12(GROCERY_SHOPPING, COMPLAIN_AND_HANDLE_RESPONSES, RELATIVE_CLAUSES),

    // ====================================
    // DIRECTIONS AND LOCATIONS (13–18)
    // ====================================
    L13(DIRECTIONS_AND_LOCATIONS, ASK_FOR_AND_GIVE_DIRECTIONS, IMPERATIVES),
    L14(DIRECTIONS_AND_LOCATIONS, DESCRIBE_OBJECTS_AND_CLARIFY, BASIC_PREPOSITIONS),
    L15(DIRECTIONS_AND_LOCATIONS, GIVE_AND_FOLLOW_INSTRUCTIONS, PRESENT_TENSE),
    L16(DIRECTIONS_AND_LOCATIONS, ASK_FOR_AND_GIVE_HELP, MODAL_VERBS_BASIC),
    L17(DIRECTIONS_AND_LOCATIONS, DESCRIBE_OBJECTS_AND_CLARIFY, RELATIVE_CLAUSES),
    L18(DIRECTIONS_AND_LOCATIONS, GIVE_AND_FOLLOW_INSTRUCTIONS, SUBORDINATE_CLAUSES),

    // =========================
    // TRAIN STATION (19–24)
    // =========================
    L19(TRAIN_STATION, ASK_FOR_AND_GIVE_DIRECTIONS, WH_QUESTIONS),
    L20(TRAIN_STATION, CONFIRM_AND_CHECK_INFORMATION, YES_NO_QUESTIONS),
    L21(TRAIN_STATION, MAKE_REQUESTS_AND_RESPOND, MODAL_VERBS_BASIC),
    L22(TRAIN_STATION, TALK_ABOUT_FUTURE_PLANS, FUTURE_FORMS),
    L23(TRAIN_STATION, DESCRIBE_AND_CLARIFY_PROCESSES, SEPARABLE_VERBS),
    L24(TRAIN_STATION, EXPLAIN_AND_DISCUSS_PROBLEMS, PAST_TENSES),

    // =========================
    // PUBLIC TRANSPORT (25–30)
    // =========================
    L25(PUBLIC_TRANSPORT, ASK_AND_ANSWER_SIMPLE_QUESTIONS, PRESENT_TENSE),
    L26(PUBLIC_TRANSPORT, GIVE_AND_FOLLOW_INSTRUCTIONS, IMPERATIVES),
    L27(PUBLIC_TRANSPORT, CONFIRM_AND_CHECK_INFORMATION, MODAL_VERBS_BASIC),
    L28(PUBLIC_TRANSPORT, TALK_ABOUT_FUTURE_PLANS, FUTURE_FORMS),
    L29(PUBLIC_TRANSPORT, DESCRIBE_AND_CLARIFY_PROCESSES, SUBORDINATE_CLAUSES),
    L30(PUBLIC_TRANSPORT, COMPARE_AND_DISCUSS_COMPARISONS, ADVANCED_PREPOSITIONS),

    // ================================
    // BANK ACCOUNT OPENING (31–36)
    // ================================
    L31(BANK_ACCOUNT_OPENING, ASK_FOR_AND_GIVE_CLARIFICATION, INDIRECT_QUESTIONS),
    L32(BANK_ACCOUNT_OPENING, MAKE_AND_CONFIRM_APPOINTMENTS, SIMPLE_TIME_EXPRESSIONS),
    L33(BANK_ACCOUNT_OPENING, COMPLAIN_AND_HANDLE_RESPONSES, RELATIVE_CLAUSES),
    L34(BANK_ACCOUNT_OPENING, CONFIRM_AND_CHECK_INFORMATION, PASSIVE_BASIC),
    L35(BANK_ACCOUNT_OPENING, EXPRESS_AND_DISCUSS_OPINIONS, ADJECTIVE_DECLENSION),
    L36(BANK_ACCOUNT_OPENING, NEGOTIATE_AND_REACH_AGREEMENT, SIMPLE_CONDITIONALS),

    // ================================
    // APARTMENT VIEWING (37–42)
    // ================================
    L37(APARTMENT_VIEWING, ASK_FOR_AND_GIVE_CLARIFICATION, WH_QUESTIONS),
    L38(APARTMENT_VIEWING, DESCRIBE_OBJECTS_AND_CLARIFY, NOUN_ADJECTIVE_BASICS),
    L39(APARTMENT_VIEWING, GIVE_AND_FOLLOW_INSTRUCTIONS, MODAL_VERBS_BASIC),
    L40(APARTMENT_VIEWING, EXPLAIN_AND_DISCUSS_PROBLEMS, SUBORDINATE_CLAUSES),
    L41(APARTMENT_VIEWING, NEGOTIATE_AND_REACH_AGREEMENT, RELATIVE_CLAUSES),
    L42(APARTMENT_VIEWING, TALK_ABOUT_FUTURE_PLANS, FUTURE_FORMS),

    // ============================
    // RENT NEGOTIATION (43–48)
    // ============================
    L43(RENT_NEGOTIATION, NEGOTIATE_AND_REACH_AGREEMENT, SIMPLE_CONDITIONALS),
    L44(RENT_NEGOTIATION, EXPRESS_AND_DISCUSS_OPINIONS, ADJECTIVE_DECLENSION),
    L45(RENT_NEGOTIATION, EXPLAIN_AND_DISCUSS_PROBLEMS, PASSIVE_BASIC),
    L46(RENT_NEGOTIATION, CONFIRM_AND_CHECK_INFORMATION, INDIRECT_QUESTIONS),
    L47(RENT_NEGOTIATION, COMPARE_AND_DISCUSS_COMPARISONS, ADVANCED_PREPOSITIONS),
    L48(RENT_NEGOTIATION, MAKE_REQUESTS_AND_RESPOND, WORD_ORDER_COMPLEX),

    // ============================
    // RENTAL CONTRACT (49–54)
    // ============================
    L49(RENTAL_CONTRACT, CONFIRM_AND_CHECK_INFORMATION, RELATIVE_CLAUSES),
    L50(RENTAL_CONTRACT, EXPLAIN_AND_DISCUSS_PROBLEMS, SUBORDINATE_CLAUSES),
    L51(RENTAL_CONTRACT, NEGOTIATE_AND_REACH_AGREEMENT, PASSIVE_BASIC),
    L52(RENTAL_CONTRACT, EXPRESS_AND_DISCUSS_OPINIONS, ADJECTIVE_DECLENSION),
    L53(RENTAL_CONTRACT, COMPLAIN_AND_HANDLE_RESPONSES, SIMPLE_CONDITIONALS),
    L54(RENTAL_CONTRACT, TALK_ABOUT_FUTURE_PLANS, FUTURE_FORMS),

    // ====================================
    // LANDLORD COMMUNICATION (55–60)
    // ====================================
    L55(LANDLORD_COMMUNICATION, ASK_FOR_AND_GIVE_HELP, MODAL_VERBS_BASIC),
    L56(LANDLORD_COMMUNICATION, ASK_FOR_AND_GIVE_CLARIFICATION, INDIRECT_QUESTIONS),
    L57(LANDLORD_COMMUNICATION, DESCRIBE_AND_CLARIFY_PROCESSES, SUBORDINATE_CLAUSES),
    L58(LANDLORD_COMMUNICATION, COMPLAIN_AND_HANDLE_RESPONSES, RELATIVE_CLAUSES),
    L59(LANDLORD_COMMUNICATION, EXPLAIN_AND_DISCUSS_PROBLEMS, PASSIVE_BASIC),
    L60(LANDLORD_COMMUNICATION, NEGOTIATE_AND_REACH_AGREEMENT, ADJECTIVE_DECLENSION);

    // =========================
    // FIELDS + CONSTRUCTOR
    // =========================

    private final GermanAdaptive.ScenarioEnum scenario;
    private final GermanAdaptive.CommunicativeFunctionEnum function;
    private final GermanAdaptive.GrammarRuleEnum rule;

    GermanBlitz(GermanAdaptive.ScenarioEnum scenario,
                GermanAdaptive.CommunicativeFunctionEnum function,
                GermanAdaptive.GrammarRuleEnum rule) {
        this.scenario = scenario;
        this.function = function;
        this.rule = rule;
    }

    public GermanAdaptive.ScenarioEnum getScenario() {
        return scenario;
    }

    public GermanAdaptive.CommunicativeFunctionEnum getFunction() {
        return function;
    }

    public GermanAdaptive.GrammarRuleEnum getRule() {
        return rule;
    }

    public static Optional<LangConfigsAdaptive> getNextLessonToGenerateContentFor(
            List<LangConfigsAdaptive> previousLessons
    ) {
        if (previousLessons == null) {
            previousLessons = List.of();
        }

        // Create a completed-set of (scenario, rule, function)
        record Combo(
                GermanAdaptive.ScenarioEnum scenario,
                GermanAdaptive.GrammarRuleEnum grammar,
                GermanAdaptive.CommunicativeFunctionEnum function
        ) {
        }

        Set<Combo> completed = previousLessons.stream()
                .map(l -> new Combo(
                        l.scenario(),
                        l.rule(),
                        l.function()
                ))
                .collect(Collectors.toSet());

        // Iterate through the NEW Blitz syllabus (L1 → L60)
        for (GermanBlitz lesson : GermanBlitz.values()) {

            Combo c = new Combo(
                    lesson.getScenario(),
                    lesson.getRule(),
                    lesson.getFunction()
            );

            if (!completed.contains(c)) {

                LangConfigsAdaptive cfg = new LangConfigsAdaptive(
                        lesson.getRule(),
                        lesson.getFunction(),
                        lesson.getScenario(),
                        new LangConfigsAdaptive.GenerationQuantity(
                                ContentGenerationQuantity.SENTENCES.getNumber()
                        )
                );

                return Optional.of(cfg);
            }
        }

        // No remaining lessons: Blitz finished 🎉
        return Optional.empty();
    }

}
