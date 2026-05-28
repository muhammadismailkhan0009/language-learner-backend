alter table writing_practice_session
    add column feedback_text text;

alter table writing_practice_session
    add column feedback_generated_at timestamp with time zone;
