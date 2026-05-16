create table practice_vocabulary_reference (
    id varchar(255) not null,
    user_id varchar(255) not null,
    vocabulary_id varchar(255) not null,
    times_matched integer not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    primary key (id),
    constraint uk_practice_vocab_ref_user_vocab unique (user_id, vocabulary_id)
);

create index ix_practice_vocab_ref_user_id
    on practice_vocabulary_reference (user_id);

create index ix_practice_vocab_ref_vocabulary_id
    on practice_vocabulary_reference (vocabulary_id);
