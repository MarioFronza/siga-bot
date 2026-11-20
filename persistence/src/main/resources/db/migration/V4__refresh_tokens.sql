create table refresh_tokens
(
    id         uuid primary key,
    user_id    bigint      not null references users (id) on delete cascade,
    family_id  uuid        not null,
    token_hash bytea       not null unique,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null default now()
);

create index refresh_tokens_family_id_idx on refresh_tokens (family_id);
create index refresh_tokens_user_id_idx on refresh_tokens (user_id);
