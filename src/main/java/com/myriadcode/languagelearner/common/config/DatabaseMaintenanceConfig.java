package com.myriadcode.languagelearner.common.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMaintenanceConfig {

    @Bean
    CommandLineRunner relaxFlashcardReviewContentTypeConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute(
                    "ALTER TABLE flashcard_review " +
                            "DROP CONSTRAINT IF EXISTS flashcard_review_content_type_check"
            );
            jdbcTemplate.execute(
                    "ALTER TABLE flashcard_review " +
                            "ADD CONSTRAINT flashcard_review_content_type_check " +
                            "CHECK (content_type IN ('CHUNK','SENTENCE','VOCABULARY'))"
            );
        };
    }
}
