create table practice_vocabulary_extraction_request (
    user_id varchar(255) not null,
    source_text text not null,
    created_at timestamp with time zone not null,
    primary key (user_id),
    constraint fk_practice_vocabulary_extraction_request_user
        foreign key (user_id) references user_info (id) on delete cascade
);
