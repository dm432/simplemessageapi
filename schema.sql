create table if not exists "user"
(
    id       bigserial not null,
    username text      not null,
    password text      not null,
    roles    text[]    not null,
    active   boolean   not null,
    unique (username),
    constraint pk_user_id primary key (id)
);

create table if not exists message
(
    id      bigserial not null,
    created timestamp not null,
    sender  bigint    not null,
    recipient    bigint    not null,
    message text      not null,
    constraint pk_message_id primary key (id),
    foreign key (sender) references "user" (id),
    foreign key (recipient) references "user" (id)
);
