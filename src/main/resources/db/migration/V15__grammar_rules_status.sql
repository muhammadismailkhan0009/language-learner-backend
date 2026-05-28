alter table grammar_rules
    add column if not exists status varchar(50);

update grammar_rules
set status = 'READY'
where status is null or btrim(status) = '';

alter table grammar_rules
    alter column status set not null;
