create table writing_practice_scenario (
    id varchar(255) not null,
    session_id varchar(255) not null,
    scenario_position integer not null,
    topic varchar(255) not null,
    english_paragraph text not null,
    german_paragraph text not null,
    submitted_answer text,
    submitted_at timestamp with time zone,
    feedback_text text,
    structured_feedback_json text,
    feedback_generated_at timestamp with time zone,
    created_at timestamp with time zone not null,
    primary key (id),
    constraint fk_writing_practice_scenario_session foreign key (session_id)
        references writing_practice_session (id) on delete cascade,
    constraint uq_writing_practice_scenario_position unique (session_id, scenario_position)
);

create index ix_writing_practice_scenario_session on writing_practice_scenario (session_id);

insert into writing_practice_scenario (
    id, session_id, scenario_position, topic, english_paragraph, german_paragraph,
    submitted_answer, submitted_at, feedback_text, structured_feedback_json,
    feedback_generated_at, created_at
)
select id || '-scenario-1', id, 0, topic, english_paragraph, german_paragraph,
       submitted_answer, submitted_at, feedback_text, structured_feedback_json,
       feedback_generated_at, created_at
from writing_practice_session;

alter table writing_practice_sentence_pair add column scenario_id varchar(255);
update writing_practice_sentence_pair set scenario_id = session_id || '-scenario-1';
alter table writing_practice_sentence_pair alter column scenario_id set not null;
alter table writing_practice_sentence_pair add constraint fk_writing_sentence_pair_scenario
    foreign key (scenario_id) references writing_practice_scenario (id) on delete cascade;
create index ix_writing_sentence_pair_scenario on writing_practice_sentence_pair (scenario_id);

alter table writing_practice_vocab_ref add column scenario_id varchar(255);
update writing_practice_vocab_ref set scenario_id = session_id || '-scenario-1';
alter table writing_practice_vocab_ref alter column scenario_id set not null;
alter table writing_practice_vocab_ref add constraint fk_writing_vocab_ref_scenario
    foreign key (scenario_id) references writing_practice_scenario (id) on delete cascade;
create index ix_writing_vocab_ref_scenario on writing_practice_vocab_ref (scenario_id);

alter table writing_practice_grammar_issue_analytics add column scenario_id varchar(255);
update writing_practice_grammar_issue_analytics set scenario_id = session_id || '-scenario-1';
alter table writing_practice_grammar_issue_analytics alter column scenario_id set not null;
alter table writing_practice_grammar_issue_analytics add constraint fk_writing_grammar_analytics_scenario
    foreign key (scenario_id) references writing_practice_scenario (id) on delete cascade;
create index ix_writing_grammar_analytics_scenario on writing_practice_grammar_issue_analytics (scenario_id);
