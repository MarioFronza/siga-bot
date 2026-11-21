alter table notifications
    add column kind text not null default 'ANNOUNCEMENT' check (kind in ('GRADE_POSTED', 'ANNOUNCEMENT'));

alter table notifications
    alter column kind drop default;
