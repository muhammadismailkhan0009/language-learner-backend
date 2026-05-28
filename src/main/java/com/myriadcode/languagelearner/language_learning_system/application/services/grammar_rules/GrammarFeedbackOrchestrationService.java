package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarFeedbackIssueResult;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrammarFeedbackOrchestrationService {

    private final GrammarRuleRepo grammarRuleRepo;

    public List<GrammarRuleCatalogItem> buildCatalog() {
        return grammarRuleRepo.findAll().stream()
                .filter(rule -> rule.active())
                .map(rule -> new GrammarRuleCatalogItem(
                        rule.identifier(),
                        rule.name(),
                        rule.explanationParagraphs().isEmpty() ? "" : rule.explanationParagraphs().get(0).text()
                ))
                .toList();
    }

    public String appendGrammarExplanations(String baseFeedback, List<GrammarFeedbackIssueResult> issues) {
        var notes = resolveIssueNotes(issues).stream()
                .filter(text -> text != null && !text.isBlank())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
        if (notes.isEmpty()) {
            return baseFeedback;
        }

        var feedback = firstNonBlank(baseFeedback, "Submission checked.");
        return feedback + "\n\nGrammar notes: " + String.join(" | ", notes);
    }

    private List<String> resolveIssueNotes(List<GrammarFeedbackIssueResult> issues) {
        if (issues == null || issues.isEmpty()) {
            return List.of();
        }

        var notes = new java.util.ArrayList<String>();
        for (var issue : issues) {
            var maybeRule = findKnownRule(issue.ruleIdentifier());
            if (maybeRule.isPresent()) {
                var explanation = maybeRule.get().explanationParagraphs() == null || maybeRule.get().explanationParagraphs().isEmpty()
                        ? ""
                        : firstNonBlank(maybeRule.get().explanationParagraphs().getFirst().text(), "");
                var core = firstNonBlank(issue.message(), issue.issueText());
                notes.add(explanation.isBlank() ? core : core + " " + explanation);
                continue;
            }

            var core = firstNonBlank(issue.message(), issue.issueText());
            var fallback = firstNonBlank(issue.fallbackExplanation(), "Grammar pattern should be adjusted in this context.");
            notes.add(core.isBlank() ? fallback : core + " " + fallback);
        }

        return notes;
    }

    private Optional<com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule> findKnownRule(String ruleIdentifier) {
        var normalized = toIdentifier(ruleIdentifier);
        if (normalized.isBlank()) {
            return Optional.empty();
        }
        return grammarRuleRepo.findAll().stream()
                .filter(rule -> rule.active())
                .filter(rule -> normalized.equals(toIdentifier(rule.identifier())))
                .findFirst();
    }

    private String toIdentifier(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String firstNonBlank(String first, String fallback) {
        return (first == null || first.isBlank()) ? fallback : first.trim();
    }
}
