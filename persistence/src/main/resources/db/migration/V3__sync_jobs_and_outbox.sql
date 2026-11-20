create table sync_jobs
(
    user_id      bigint primary key references users (id) on delete cascade,
    next_run_at  timestamptz not null default now(),
    locked_until timestamptz,
    attempts     integer     not null default 0 check (attempts >= 0),
    last_error   text
);

create index sync_jobs_next_run_at_idx on sync_jobs (next_run_at);

insert into sync_jobs (user_id)
select id
from users;

create table outbox
(
    id           bigint generated always as identity primary key,
    user_id      bigint      not null references users (id) on delete cascade,
    message         text        not null,
    attempts        integer     not null default 0 check (attempts >= 0),
    next_attempt_at timestamptz not null default now(),
    locked_until    timestamptz,
    created_at      timestamptz not null default now(),
    delivered_at    timestamptz
);

create index outbox_pending_idx on outbox (next_attempt_at) where delivered_at is null;
