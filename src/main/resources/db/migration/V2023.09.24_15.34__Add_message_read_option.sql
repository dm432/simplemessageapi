alter table message
    add column read boolean not null default false,
    add unique (created, sender, recipient, message);