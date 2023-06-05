create table if not exists "user"
(
    id       bigserial not null,
    username text      not null,
    password text      not null,
    roles    text[]    not null,
    active   boolean   not null,
    unique (username),
    constraint pk_user_id primary key (id)
)