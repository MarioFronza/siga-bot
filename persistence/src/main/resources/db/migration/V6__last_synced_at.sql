alter table sync_jobs
    add column last_synced_at timestamptz;
