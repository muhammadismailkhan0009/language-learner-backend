package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import java.util.List;

public record VocabularyCandidateSelection(String requestId, List<Candidate> candidates) {
    public record Candidate(String surface) {}
}
