alter table sync_jobs
    add column last_error_reason text check (last_error_reason in ('INVALID_CREDENTIAL', 'SIGA_UNAVAILABLE', 'SIGA_CHANGED', 'UNKNOWN'));

update sync_jobs
set last_error_reason = 'UNKNOWN'
where last_error is not null;

alter table sync_jobs
    drop column last_error;
