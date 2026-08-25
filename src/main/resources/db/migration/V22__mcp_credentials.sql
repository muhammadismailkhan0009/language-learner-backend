create table mcp_credentials (
    user_id varchar(255) not null,
    secret_key varchar(255) not null,
    created_at timestamp with time zone not null,
    primary key (user_id),
    constraint uk_mcp_credentials_secret_key unique (secret_key),
    constraint fk_mcp_credentials_user
        foreign key (user_id) references user_info (id)
);
