create table reading_practice_scenario (
    id varchar(255) not null,
    session_id varchar(255) not null,
    scenario_index integer not null,
    scenario_label varchar(255) not null,
    reading_text text not null,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_reading_scenario_session foreign key (session_id) references reading_practice_session (id)
);

insert into reading_practice_scenario (id, session_id, scenario_index, scenario_label, reading_text, created_at)
select id, id, 0, topic, reading_text, created_at from reading_practice_session;

alter table reading_practice_paragraph add column scenario_id varchar(255);
update reading_practice_paragraph set scenario_id = session_id;
alter table reading_practice_paragraph alter column scenario_id set not null;
alter table reading_practice_paragraph add constraint fk_reading_paragraph_scenario foreign key (scenario_id) references reading_practice_scenario (id);
drop index ix_reading_practice_paragraph_order;
drop index ix_reading_practice_paragraph_session;
alter table reading_practice_paragraph drop constraint fk_reading_practice_paragraph_session;
alter table reading_practice_paragraph drop column session_id;
create index ix_reading_practice_paragraph_order on reading_practice_paragraph (scenario_id, paragraph_index);

alter table reading_practice_vocab_ref add column scenario_id varchar(255);
update reading_practice_vocab_ref set scenario_id = session_id;
alter table reading_practice_vocab_ref alter column scenario_id set not null;
alter table reading_practice_vocab_ref add constraint fk_reading_vocab_scenario foreign key (scenario_id) references reading_practice_scenario (id);
drop index ix_reading_practice_vocab_ref_session;
alter table reading_practice_vocab_ref drop constraint fk_reading_practice_session;
alter table reading_practice_vocab_ref drop column session_id;
create index ix_reading_practice_vocab_ref_scenario on reading_practice_vocab_ref (scenario_id);
create index ix_reading_practice_scenario_order on reading_practice_scenario (session_id, scenario_index);
