alter table writing_practice_session
    add column structured_feedback_json text;

create table writing_practice_grammar_issue_analytics (
    id varchar(255) not null,
    session_id varchar(255) not null,
    user_id varchar(255) not null,
    grammar_rule_identifier varchar(255),
    issue_type varchar(255) not null,
    priority integer not null,
    learner_text text,
    corrected_text text,
    short_explanation text,
    occurrence_count integer not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_writing_grammar_issue_analytics_session
        foreign key (session_id)
            references writing_practice_session (id)
            on delete cascade
);

create index ix_writing_grammar_issue_analytics_session
    on writing_practice_grammar_issue_analytics (session_id);

create index ix_writing_grammar_issue_analytics_user
    on writing_practice_grammar_issue_analytics (user_id);
