package com.myriadcode.languagelearner.language_content.application.ports;

import java.util.List;

public record ReadingTopicCandidates(
        List<String> topics
) {
}
