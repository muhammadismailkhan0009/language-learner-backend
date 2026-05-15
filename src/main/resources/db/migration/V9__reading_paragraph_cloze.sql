create table reading_paragraph_cloze_session (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    topic varchar(512) not null,
    cloze_paragraph text not null,
    created_at timestamp not null
);

create index ix_reading_paragraph_cloze_session_user on reading_paragraph_cloze_session (user_id, created_at desc);

create table reading_paragraph_cloze_card (
    id varchar(64) primary key,
    session_id varchar(64) not null,
    flashcard_id varchar(64) not null,
    vocabulary_id varchar(64) not null,
    created_at timestamp not null,
    constraint fk_reading_paragraph_cloze_card_session
        foreign key (session_id)
            references reading_paragraph_cloze_session (id)
            on delete cascade
);

create index ix_reading_paragraph_cloze_card_session on reading_paragraph_cloze_card (session_id);
create index ix_reading_paragraph_cloze_card_flashcard on reading_paragraph_cloze_card (flashcard_id);
