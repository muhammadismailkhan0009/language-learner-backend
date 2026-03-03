alter table words_to_listen_to
    add column if not exists user_id varchar(255);

update words_to_listen_to
set user_id = ''
where user_id is null;

alter table words_to_listen_to
    alter column user_id set not null;
