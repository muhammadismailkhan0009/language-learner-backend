create table reading_practice_session (
    id varchar(255) not null,
    user_id varchar(255) not null,
    topic varchar(255) not null,
    reading_text text not null,
    created_at timestamp with time zone not null,
    primary key (id)
);

create index ix_reading_practice_session_user_id
    on reading_practice_session (user_id);

create table reading_practice_vocab_ref (
    id varchar(255) not null,
    session_id varchar(255) not null,
    flashcard_id varchar(255) not null,
    vocabulary_id varchar(255) not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_reading_practice_session
        foreign key (session_id)
            references reading_practice_session (id)
);

create index ix_reading_practice_vocab_ref_session
    on reading_practice_vocab_ref (session_id);
