alter table grammar_rules
    add column if not exists identifier varchar(255);

update grammar_rules
set identifier = lower(regexp_replace(name, '[^a-z0-9]+', '-', 'g'))
where identifier is null or btrim(identifier) = '';

alter table grammar_rules
    alter column identifier set not null;

create unique index if not exists uq_grammar_rules_identifier
    on grammar_rules (identifier);

alter table grammar_rules
    add column if not exists level varchar(50);

update grammar_rules
set level = 'A1'
where level is null or btrim(level) = '';

alter table grammar_rules
    alter column level set not null;

alter table grammar_rules
    add column if not exists active boolean;

update grammar_rules
set active = true
where active is null;

alter table grammar_rules
    alter column active set not null;
