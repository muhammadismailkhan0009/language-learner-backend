create table reading_paragraph_cloze_paragraph (
    id varchar(64) primary key,
    session_id varchar(64) not null,
    paragraph_index integer not null,
    scenario_label varchar(256) not null,
    cloze_paragraph text not null,
    created_at timestamp not null,
    constraint fk_reading_paragraph_cloze_paragraph_session
        foreign key (session_id)
            references reading_paragraph_cloze_session (id)
            on delete cascade
);

create index ix_reading_paragraph_cloze_paragraph_session on reading_paragraph_cloze_paragraph (session_id, paragraph_index asc);

alter table reading_paragraph_cloze_card
    add column if not exists paragraph_id varchar(64);

alter table reading_paragraph_cloze_card
    add constraint fk_reading_paragraph_cloze_card_paragraph
        foreign key (paragraph_id)
            references reading_paragraph_cloze_paragraph (id)
            on delete cascade;

create index ix_reading_paragraph_cloze_card_paragraph on reading_paragraph_cloze_card (paragraph_id);
