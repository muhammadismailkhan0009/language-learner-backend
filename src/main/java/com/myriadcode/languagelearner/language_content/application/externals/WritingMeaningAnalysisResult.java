package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingMeaningAnalysisResult(
        String overallCoverage,
        List<String> coveredIdeas,
        List<String> missedIdeas,
        List<String> distortedIdeas,
        List<IdeaAlignment> alignments
) {
    public record IdeaAlignment(String promptIdea, String learnerText) {
    }
}
