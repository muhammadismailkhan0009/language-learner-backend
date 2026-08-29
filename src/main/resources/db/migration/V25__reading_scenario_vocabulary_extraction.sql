alter table content_generation_jobs drop constraint content_generation_jobs_pkey;
alter table content_generation_jobs add primary key (user_id, type);

alter table reading_practice_scenario
    add column rated_cards_count integer not null default 0,
    add column all_cards_rated boolean not null default false,
    add constraint ck_reading_scenario_rated_cards_count check (rated_cards_count >= 0);

create table vocabulary_extraction_request (
    id varchar(255) not null,
    user_id varchar(255) not null,
    source_text text not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_vocab_extraction_request_user foreign key (user_id) references user_info (id)
);

create index ix_vocab_extraction_request_user_created
    on vocabulary_extraction_request (user_id, created_at);

create table vocabulary_extraction_candidate (
    id varchar(255) not null,
    user_id varchar(255) not null,
    surface varchar(500) not null,
    normalized_surface varchar(500) not null,
    created_vocabulary_id varchar(255),
    created_at timestamp with time zone not null,
    primary key (id),
    constraint uq_vocab_extraction_candidate unique (user_id, normalized_surface),
    constraint fk_vocab_extraction_candidate_user foreign key (user_id) references user_info (id),
    constraint fk_vocab_extraction_candidate_vocabulary foreign key (created_vocabulary_id)
        references vocabulary_entries (id) on delete cascade
);

create index ix_vocab_extraction_candidate_pending
    on vocabulary_extraction_candidate (user_id, created_at)
    where created_vocabulary_id is null;
