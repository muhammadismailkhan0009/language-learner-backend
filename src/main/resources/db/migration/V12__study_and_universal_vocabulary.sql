create table universal_vocabulary_pool (
    id varchar(255) not null,
    normalized_surface varchar(255) not null,
    surface varchar(255) not null,
    entry_kind varchar(64) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    primary key (id),
    constraint uk_universal_vocab_surface_kind unique (normalized_surface, entry_kind)
);

create table study_sentence_pool (
    id varchar(255) not null,
    vocabulary_id varchar(255) not null,
    sentence_text_with_blank text not null,
    true_answer_surface varchar(255) not null,
    normalized_true_answer varchar(255) not null,
    hint varchar(255),
    source varchar(64) not null,
    created_at timestamp with time zone not null,
    primary key (id)
);

create index ix_study_sentence_pool_vocab_id
    on study_sentence_pool (vocabulary_id);

create table study_user_sentence_usage (
    id varchar(255) not null,
    user_id varchar(255) not null,
    sentence_id varchar(255) not null,
    first_seen_at timestamp with time zone not null,
    last_seen_at timestamp with time zone not null,
    times_shown integer not null,
    times_correct integer not null,
    times_wrong integer not null,
    primary key (id),
    constraint uk_study_user_sentence unique (user_id, sentence_id)
);

create index ix_study_user_sentence_usage_user
    on study_user_sentence_usage (user_id);

create table study_session (
    id varchar(255) not null,
    user_id varchar(255) not null,
    status varchar(64) not null,
    created_at timestamp with time zone not null,
    completed_at timestamp with time zone,
    primary key (id)
);

create index ix_study_session_user
    on study_session (user_id);

create table study_session_item (
    id varchar(255) not null,
    session_id varchar(255) not null,
    flashcard_id varchar(255) not null,
    vocabulary_id varchar(255) not null,
    sentence_id varchar(255) not null,
    queue_rank_snapshot integer not null,
    presented_at timestamp with time zone not null,
    rated_at timestamp with time zone,
    rating_applied varchar(32),
    answer_text text,
    evaluation_mode varchar(32),
    feedback_text text,
    primary key (id),
    constraint fk_study_session_item_session
        foreign key (session_id) references study_session (id)
);

create index ix_study_session_item_session
    on study_session_item (session_id);

create table study_answer_log (
    id varchar(255) not null,
    session_item_id varchar(255) not null,
    user_answer text not null,
    normalized_user_answer varchar(255) not null,
    is_exact_match boolean not null,
    llm_payload_json text,
    mapped_rating varchar(32) not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_study_answer_log_item
        foreign key (session_item_id) references study_session_item (id)
);

create index ix_study_answer_log_item
    on study_answer_log (session_item_id);
