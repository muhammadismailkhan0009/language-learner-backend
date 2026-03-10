create table if not exists vocabulary_cloze_sentences (
    id varchar(255) primary key,
    entry_id varchar(255) not null unique,
    cloze_text varchar(2000) not null,
    hint varchar(500) not null,
    answer_text varchar(500) not null,
    answer_words_json text not null,
    answer_translation varchar(500) not null,
    created_at timestamp with time zone not null,
    constraint fk_vocabulary_cloze_sentences_entry
        foreign key (entry_id) references vocabulary_entries (id) on delete cascade
);
