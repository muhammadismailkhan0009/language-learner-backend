package com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze;

import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGenerationContext;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingVocabularySelectionService;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services.GrammarRuleVisibilityPolicy;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import org.springframework.stereotype.Service;

@Service
public class ReadingParagraphClozeGenerationContextService {
    private final UserDifficultyLevelApi levelApi;
    private final GrammarRuleRepo grammarRuleRepo;
    private final WritingVocabularySelectionService vocabularySelectionService;

    public ReadingParagraphClozeGenerationContextService(UserDifficultyLevelApi levelApi,
                                                         GrammarRuleRepo grammarRuleRepo,
                                                         WritingVocabularySelectionService vocabularySelectionService) {
        this.levelApi = levelApi;
        this.grammarRuleRepo = grammarRuleRepo;
        this.vocabularySelectionService = vocabularySelectionService;
    }

    public ClozeParagraphGenerationContext build(String userId, Integer limit) {
        var level = levelApi.getDifficultyLevel(userId);
        var vocabulary = vocabularySelectionService.select(userId, limit).stream()
                .map(item -> new ClozeParagraphGenerationContext.VocabularySource(
                        item.id(), item.surface(), item.translation(), item.entryKind(), item.notes()))
                .toList();
        var grammar = grammarRuleRepo.findAll().stream()
                .filter(rule -> GrammarRuleVisibilityPolicy.isVisibleTo(rule, level))
                .map(this::toGrammarSource)
                .toList();
        return new ClozeParagraphGenerationContext(level, vocabulary, grammar);
    }

    private ClozeParagraphGenerationContext.GrammarSource toGrammarSource(GrammarRule rule) {
        var paragraphs = rule.explanationParagraphs() == null ? java.util.List.<String>of()
                : rule.explanationParagraphs().stream().map(value -> value.text()).toList();
        var examples = rule.grammarScenario() == null || rule.grammarScenario().sentences() == null
                ? java.util.List.<ClozeParagraphGenerationContext.Example>of()
                : rule.grammarScenario().sentences().stream()
                .map(value -> new ClozeParagraphGenerationContext.Example(value.sentence(), value.translation())).toList();
        return new ClozeParagraphGenerationContext.GrammarSource(
                rule.id().id(), rule.identifier(), rule.name(), rule.level(), paragraphs, examples);
    }
}
