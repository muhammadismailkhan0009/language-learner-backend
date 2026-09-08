create table word_practices (
    id varchar(255) primary key,
    user_id varchar(255) not null,
    vocabulary_id varchar(255) not null references vocabulary_entries(id),
    direction varchar(32) not null,
    source_sentence text not null,
    cloze_sentence text not null,
    complete_sentence text not null,
    exact_answer text not null,
    accepted_answers jsonb not null,
    created_at timestamp with time zone not null
);

create index ix_word_practices_user_created on word_practices(user_id, created_at, id);
create index ix_word_practices_user_vocabulary on word_practices(user_id, vocabulary_id);

create table word_practice_consumed_weak_events (
    user_id varchar(255) not null,
    weak_event_id varchar(1000) not null,
    vocabulary_id varchar(255) not null references vocabulary_entries(id),
    consumed_at timestamp with time zone not null,
    primary key (user_id, weak_event_id)
);
