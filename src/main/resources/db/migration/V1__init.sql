create table user_info (
    id varchar(255) not null,
    username varchar(255),
    password varchar(255),
    primary key (id)
);

create table flashcard_review (
    id varchar(255) not null,
    user_id varchar(255) not null,
    language_content_id varchar(255) not null,
    content_type varchar(255) not null,
    card_json jsonb not null,
    is_reversed boolean default false not null,
    primary key (id),
    constraint uq_flashcard_review_content unique (language_content_id, content_type, user_id, is_reversed)
);

create table sentences (
    id varchar(255) not null,
    sentence varchar(255),
    translation varchar(255),
    scenario varchar(255) not null,
    grammar_rule varchar(255) not null,
    communication_function varchar(255) not null,
    primary key (id)
);

create index ix_sentences_scenario_grammar_function
    on sentences (scenario, grammar_rule, communication_function);

create table chunks (
    id varchar(255) not null,
    chunk varchar(255),
    translation varchar(255),
    note varchar(255),
    primary key (id)
);

create table user_stats_for_content_entity (
    id varchar(255) not null,
    user_id varchar(255),
    scenario varchar(255),
    function varchar(255),
    grammar_rule varchar(255),
    syllabus_assigned_at timestamp,
    primary key (id)
);

create table vocabulary_entries (
    id varchar(255) not null,
    user_id varchar(255) not null,
    surface varchar(255) not null,
    translation varchar(255) not null,
    entry_kind varchar(255) not null,
    notes varchar(5000),
    schema_version integer not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    primary key (id)
);

create table vocabulary_example_sentences (
    id varchar(255) not null,
    entry_id varchar(255) not null,
    sentence varchar(255) not null,
    translation varchar(255) not null,
    display_order integer not null,
    primary key (id),
    constraint fk_vocab_example_entry
        foreign key (entry_id)
            references vocabulary_entries (id)
);

create table words_to_listen_to (
    id varchar(255) not null,
    word varchar(255) not null,
    user_id varchar(255) not null,
    primary key (id)
);

create table public_vocabularies (
    id varchar(255) not null,
    source_vocabulary_id varchar(255) not null,
    published_by_user_id varchar(255) not null,
    status varchar(255) not null,
    published_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    primary key (id),
    constraint uq_public_vocabularies_source unique (source_vocabulary_id)
);

create table grammar_scenarios (
    id varchar(255) not null,
    title varchar(255) not null,
    description varchar(5000) not null,
    target_language varchar(255) not null,
    created_by varchar(255) not null,
    is_fixed boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    primary key (id)
);

create table grammar_rules (
    id varchar(255) not null,
    name varchar(255) not null,
    grammar_scenario_id varchar(255) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    primary key (id),
    constraint uq_grammar_rules_scenario unique (grammar_scenario_id),
    constraint fk_grammar_rules_scenario
        foreign key (grammar_scenario_id)
            references grammar_scenarios (id)
);

create table grammar_rule_explanation_paragraphs (
    id varchar(255) not null,
    grammar_rule_id varchar(255) not null,
    paragraph_text varchar(5000) not null,
    display_order integer not null,
    primary key (id),
    constraint fk_grammar_rule_paragraphs
        foreign key (grammar_rule_id)
            references grammar_rules (id)
);

create table grammar_scenario_sentences (
    id varchar(255) not null,
    grammar_scenario_id varchar(255) not null,
    sentence varchar(255) not null,
    translation varchar(255) not null,
    display_order integer not null,
    primary key (id),
    constraint fk_grammar_scenario_sentences
        foreign key (grammar_scenario_id)
            references grammar_scenarios (id)
);

create table scenarios (
    id varchar(255) not null,
    user_id varchar(255) not null,
    nature varchar(255) not null,
    target_language varchar(255) not null,
    primary key (id)
);

create table scenario_sentences (
    id varchar(255) not null,
    scenario_id varchar(255) not null,
    sentence varchar(255) not null,
    translation varchar(255) not null,
    display_order integer not null,
    primary key (id),
    constraint fk_scenario_sentences
        foreign key (scenario_id)
            references scenarios (id)
);

CREATE TABLE IF NOT EXISTS event_publication
(
    id                     UUID NOT NULL,
    listener_id            TEXT NOT NULL,
    event_type             TEXT NOT NULL,
    serialized_event       TEXT NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
    );
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);

CREATE TABLE IF NOT EXISTS event_publication_archive
(
    id                     UUID NOT NULL,
    listener_id            TEXT NOT NULL,
    event_type             TEXT NOT NULL,
    serialized_event       TEXT NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
    );
CREATE INDEX IF NOT EXISTS event_publication_archive_serialized_event_hash_idx ON event_publication_archive USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_archive_by_completion_date_idx ON event_publication_archive (completion_date);