create table content_generation_jobs (
    user_id varchar(255) not null,
    type varchar(64) not null,
    created_at timestamp with time zone not null,
    primary key (user_id),
    constraint fk_content_generation_jobs_user
        foreign key (user_id) references user_info (id)
);
