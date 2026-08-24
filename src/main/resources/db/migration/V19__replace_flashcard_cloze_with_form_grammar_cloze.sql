delete from reading_paragraph_cloze_session;

drop table if exists reading_paragraph_cloze_card;

alter table reading_paragraph_cloze_session
    drop column topic,
    drop column cloze_paragraph,
    add column learner_level varchar(8) not null;

alter table reading_paragraph_cloze_paragraph
    drop column created_at;

create table reading_paragraph_cloze_blank (
    id varchar(255) primary key,
    paragraph_id varchar(255) not null,
    blank_index integer not null,
    blank_token varchar(255) not null,
    exact_answer text not null,
    answer_explanation text not null,
    practice_kind varchar(64) not null,
    vocabulary_id varchar(255),
    constraint fk_reading_paragraph_cloze_blank_paragraph
        foreign key (paragraph_id) references reading_paragraph_cloze_paragraph (id) on delete cascade,
    constraint uq_reading_paragraph_cloze_blank_order unique (paragraph_id, blank_index),
    constraint ck_reading_paragraph_cloze_blank_kind
        check (practice_kind in ('VOCABULARY_FORM', 'GRAMMAR', 'VOCABULARY_AND_GRAMMAR'))
);

create index ix_reading_paragraph_cloze_blank_paragraph
    on reading_paragraph_cloze_blank (paragraph_id, blank_index);

create table reading_paragraph_cloze_blank_grammar_rule (
    blank_id varchar(255) not null,
    grammar_rule_id varchar(255) not null,
    primary key (blank_id, grammar_rule_id),
    constraint fk_reading_paragraph_cloze_blank_grammar
        foreign key (blank_id) references reading_paragraph_cloze_blank (id) on delete cascade
);
