package com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.*;
import com.myriadcode.languagelearner.language_content.infra.llm.LlmUserContextHolder;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.*;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.services.ReadingParagraphClozeGenerationValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReadingParagraphClozeService {
    private final ReadingParagraphClozeRepo repo;
    private final ReadingParagraphClozeGenerationContextService contextService;
    private final ClozeParagraphLlmApi llmApi;
    private final FetchPrivateVocabularyApi vocabularyApi;
    private final GrammarRuleRepo grammarRuleRepo;
    private final ReadingParagraphClozeGenerationValidator validator = new ReadingParagraphClozeGenerationValidator();

    public ReadingParagraphClozeService(ReadingParagraphClozeRepo repo,
                                       ReadingParagraphClozeGenerationContextService contextService,
                                       ClozeParagraphLlmApi llmApi,
                                       FetchPrivateVocabularyApi vocabularyApi,
                                       GrammarRuleRepo grammarRuleRepo) {
        this.repo = repo; this.contextService = contextService; this.llmApi = llmApi;
        this.vocabularyApi = vocabularyApi; this.grammarRuleRepo = grammarRuleRepo;
    }

    @Transactional
    public ReadingParagraphClozeSessionResponse createSession(String userId, Integer limit) {
        requireUserId(userId);
        var context = contextService.build(userId, limit);
        return generateAndStore(userId, context);
    }

    @Transactional(readOnly = true)
    public ClozeParagraphGenerationContext prepareGeneration(String userId) {
        requireUserId(userId);
        return contextService.build(userId, 50);
    }

    @Transactional(readOnly = true)
    public String buildGenerationPrompt(ClozeParagraphGenerationContext context) {
        return llmApi.buildPrompt(context);
    }

    @Transactional
    public ReadingParagraphClozeSessionResponse storeGeneration(String userId, ClozeParagraphGeneration generated) {
        requireUserId(userId);
        return validateAndStore(userId, prepareGeneration(userId), generated);
    }

    private ReadingParagraphClozeSessionResponse generateAndStore(
            String userId,
            ClozeParagraphGenerationContext context
    ) {
        var vocabularyIds = context.vocabulary().stream().map(ClozeParagraphGenerationContext.VocabularySource::id).collect(Collectors.toSet());
        var grammarIds = context.grammarRules().stream().map(ClozeParagraphGenerationContext.GrammarSource::id).collect(Collectors.toSet());
        var generated = generate(context, userId);
        if (!validator.isValid(generated, vocabularyIds, grammarIds)) generated = generate(context, userId);
        return validateAndStore(userId, context, generated);
    }

    private ReadingParagraphClozeSessionResponse validateAndStore(
            String userId,
            ClozeParagraphGenerationContext context,
            ClozeParagraphGeneration generated
    ) {
        var vocabularyIds = context.vocabulary().stream().map(ClozeParagraphGenerationContext.VocabularySource::id).collect(Collectors.toSet());
        var grammarIds = context.grammarRules().stream().map(ClozeParagraphGenerationContext.GrammarSource::id).collect(Collectors.toSet());
        var validationErrors = validator.validate(generated, vocabularyIds, grammarIds);
        if (!validationErrors.isEmpty())
            throw new IllegalArgumentException("Reading paragraph cloze validation failed: " + String.join("; ", validationErrors));
        return toResponse(repo.save(toSession(userId, context, generated)), userId);
    }

    @Transactional(readOnly = true)
    public List<ReadingParagraphClozeSessionResponse> listSessions(String userId) {
        requireUserId(userId);
        return repo.findAllByUserId(userId).stream().map(session -> toResponse(session, userId)).toList();
    }

    @Transactional(readOnly = true)
    public ReadingParagraphClozeSessionResponse getSession(String userId, String sessionId) {
        requireUserId(userId);
        return toResponse(repo.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Reading paragraph cloze session not found")), userId);
    }

    @Transactional
    public void deleteSession(String userId, String sessionId) {
        requireUserId(userId);
        if (!repo.deleteByIdAndUserId(sessionId, userId))
            throw new IllegalArgumentException("Reading paragraph cloze session not found");
    }

    private ClozeParagraphGeneration generate(ClozeParagraphGenerationContext context, String userId) {
        try (var ignored = LlmUserContextHolder.scoped(userId)) { return llmApi.generate(context); }
    }

    private ReadingParagraphClozeSession toSession(String userId, ClozeParagraphGenerationContext context,
                                                   ClozeParagraphGeneration generated) {
        int paragraphIndex = 0;
        var paragraphs = new ArrayList<ReadingParagraphClozeParagraph>();
        for (var sourceParagraph : generated.paragraphs()) {
            int blankIndex = 0;
            var blanks = new ArrayList<ReadingParagraphClozeBlank>();
            for (var sourceBlank : sourceParagraph.blanks()) {
                blanks.add(new ReadingParagraphClozeBlank(
                        new ReadingParagraphClozeBlank.ReadingParagraphClozeBlankId(UUID.randomUUID().toString()),
                        blankIndex++, sourceBlank.blankToken().trim(), sourceBlank.exactAnswer().trim(),
                        sourceBlank.answerExplanation().trim(),
                        sourceBlank.practiceKind(),
                        sourceBlank.vocabularyId(), sourceBlank.grammarRuleIds()));
            }
            paragraphs.add(new ReadingParagraphClozeParagraph(
                    new ReadingParagraphClozeParagraph.ReadingParagraphClozeParagraphId(UUID.randomUUID().toString()),
                    paragraphIndex++, sourceParagraph.scenarioLabel(), sourceParagraph.clozeParagraph().trim(), blanks));
        }
        return new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId(UUID.randomUUID().toString()),
                new UserId(userId), context.learnerLevel(), Instant.now(), paragraphs);
    }

    private ReadingParagraphClozeSessionResponse toResponse(ReadingParagraphClozeSession session, String userId) {
        var vocabularyIds = session.paragraphs().stream().flatMap(p -> p.blanks().stream())
                .map(ReadingParagraphClozeBlank::vocabularyId).filter(Objects::nonNull).distinct().toList();
        var vocabulary = vocabularyIds.isEmpty() ? Map.<String, PrivateVocabularyRecord>of()
                : vocabularyApi.getVocabularyRecords(vocabularyIds, userId).stream()
                .filter(item -> userId.equals(item.userId()))
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));
        var grammarIds = session.paragraphs().stream().flatMap(p -> p.blanks().stream())
                .flatMap(blank -> blank.grammarRuleIds().stream()).distinct().toList();
        var grammar = grammarIds.stream().map(grammarRuleRepo::findById).flatMap(Optional::stream)
                .collect(Collectors.toMap(rule -> rule.id().id(), Function.identity()));
        var paragraphs = session.paragraphs().stream().map(paragraph -> new ReadingParagraphClozeSessionResponse.Paragraph(
                paragraph.id().id(), paragraph.paragraphIndex(), paragraph.scenarioLabel(), paragraph.clozeParagraph(),
                paragraph.blanks().stream().map(blank -> toBlankResponse(blank, vocabulary, grammar)).toList())).toList();
        return new ReadingParagraphClozeSessionResponse(session.id().id(), session.learnerLevel().name(), session.createdAt(), paragraphs);
    }

    private ReadingParagraphClozeSessionResponse.Blank toBlankResponse(ReadingParagraphClozeBlank blank,
                                                                       Map<String, PrivateVocabularyRecord> vocabulary,
                                                                       Map<String, GrammarRule> grammar) {
        var vocab = vocabulary.get(blank.vocabularyId());
        ReadingParagraphClozeSessionResponse.VocabularyDetails vocabDetails = vocab == null ? null
                : new ReadingParagraphClozeSessionResponse.VocabularyDetails(
                vocab.id(), vocab.surface(), vocab.translation(), vocab.entryKind(), vocab.notes(),
                (vocab.exampleSentences() == null ? List.<PrivateVocabularyRecord.ExampleSentenceRecord>of() : vocab.exampleSentences())
                        .stream().map(example -> new ReadingParagraphClozeSessionResponse.ExampleSentence(
                        example.sentence(), example.translation())).toList());
        var grammarDetails = blank.grammarRuleIds().stream().map(grammar::get).filter(Objects::nonNull)
                .map(this::toGrammarDetails).toList();
        return new ReadingParagraphClozeSessionResponse.Blank(blank.id().id(), blank.blankIndex(), blank.blankToken(),
                blank.exactAnswer(), blank.answerExplanation(), blank.practiceKind().name(), vocabDetails, grammarDetails);
    }

    private ReadingParagraphClozeSessionResponse.GrammarRuleDetails toGrammarDetails(GrammarRule rule) {
        var paragraphs = rule.explanationParagraphs() == null ? List.<String>of()
                : rule.explanationParagraphs().stream().map(value -> value.text()).toList();
        var examples = rule.grammarScenario() == null || rule.grammarScenario().sentences() == null
                ? List.<ReadingParagraphClozeSessionResponse.ExampleSentence>of()
                : rule.grammarScenario().sentences().stream().map(value ->
                new ReadingParagraphClozeSessionResponse.ExampleSentence(value.sentence(), value.translation())).toList();
        return new ReadingParagraphClozeSessionResponse.GrammarRuleDetails(
                rule.id().id(), rule.identifier(), rule.name(), rule.level(), paragraphs, examples);
    }

    private void requireUserId(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("User id is required");
    }
}
