create table user_silo.users
(
    id           uuid primary key,
    keycloak_id  uuid         not null unique,
    email        varchar(255) not null unique,
    display_name varchar(255) not null,
    updated_at   timestamptz  not null,
    created_at   timestamptz  not null
);