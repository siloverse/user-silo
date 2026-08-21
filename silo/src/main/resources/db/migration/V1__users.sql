create table user_silo.users
(
    id           uuid primary key     default gen_random_uuid(),
    keycloak_id  uuid        not null unique,
    email        text        not null unique,
    display_name text        not null,
    updated_at   timestamptz not null default now(),
    created_at   timestamptz not null default now()
);