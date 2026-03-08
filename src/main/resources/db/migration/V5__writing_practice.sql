create table writing_practice_session (
    id varchar(255) not null,
    user_id varchar(255) not null,
    topic varchar(255) not null,
    english_paragraph text not null,
    german_paragraph text not null,
    submitted_answer text,
    submitted_at timestamp with time zone,
    created_at timestamp with time zone not null,
    primary key (id)
);

create index ix_writing_practice_session_user_id
    on writing_practice_session (user_id);

create table writing_practice_sentence_pair (
    id varchar(255) not null,
    session_id varchar(255) not null,
    sentence_index integer not null,
    english_sentence text not null,
    german_sentence text not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_writing_practice_sentence_pair_session
        foreign key (session_id)
            references writing_practice_session (id)
);

create index ix_writing_practice_sentence_pair_session
    on writing_practice_sentence_pair (session_id);

create index ix_writing_practice_sentence_pair_order
    on writing_practice_sentence_pair (session_id, sentence_index);

create table writing_practice_vocab_ref (
    id varchar(255) not null,
    session_id varchar(255) not null,
    flashcard_id varchar(255) not null,
    vocabulary_id varchar(255) not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_writing_practice_vocab_ref_session
        foreign key (session_id)
            references writing_practice_session (id)
);

create index ix_writing_practice_vocab_ref_session
    on writing_practice_vocab_ref (session_id);
