create table user_profiles (
    user_id varchar(255) not null,
    difficulty_level varchar(255) not null default 'A1',
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    primary key (user_id),
    constraint fk_user_profiles_user_info
        foreign key (user_id)
            references user_info (id)
);
