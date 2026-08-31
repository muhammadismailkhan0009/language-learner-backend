alter table user_profiles
    add column reading_difficulty_level varchar(255) not null default 'A1',
    add column writing_difficulty_level varchar(255) not null default 'A1';
