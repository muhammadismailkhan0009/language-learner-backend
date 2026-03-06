create table reading_practice_paragraph (
    id varchar(255) not null,
    session_id varchar(255) not null,
    paragraph_index integer not null,
    paragraph_text text not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_reading_practice_paragraph_session
        foreign key (session_id)
            references reading_practice_session (id)
);

create index ix_reading_practice_paragraph_session
    on reading_practice_paragraph (session_id);

create index ix_reading_practice_paragraph_order
    on reading_practice_paragraph (session_id, paragraph_index);

create table reading_practice_sentence (
    id varchar(255) not null,
    paragraph_id varchar(255) not null,
    sentence_index integer not null,
    sentence_text text not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_reading_practice_sentence_paragraph
        foreign key (paragraph_id)
            references reading_practice_paragraph (id)
);

create index ix_reading_practice_sentence_paragraph
    on reading_practice_sentence (paragraph_id);

create index ix_reading_practice_sentence_order
    on reading_practice_sentence (paragraph_id, sentence_index);
