-- Accounts

create table users
(
    id            bigint generated always as identity primary key,
    external_id   uuid        not null unique,
    email         text        not null unique check (email = lower(email)),
    password_hash text        not null,
    full_name     text        not null default '',
    active        boolean     not null default true,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);

create table roles
(
    id   bigint generated always as identity primary key,
    name text not null unique
);

insert into roles (name)
values ('STUDENT'),
       ('ADMIN');

create table user_roles
(
    user_id bigint not null references users (id) on delete cascade,
    role_id bigint not null references roles (id),
    primary key (user_id, role_id)
);

create index user_roles_role_id_idx on user_roles (role_id);

-- Institutions whose academic systems the bot reads. grade_scale_max tells how to read their grades.
create table institutions
(
    id              bigint generated always as identity primary key,
    code            text          not null unique,
    name            text          not null,
    grade_scale_max numeric(5, 2) not null check (grade_scale_max > 0)
);

insert into institutions (code, name, grade_scale_max)
values ('udesc', 'Universidade do Estado de Santa Catarina', 10);

-- A user's login in one institution's academic system. The login is whatever that system asks for
-- (CPF, enrollment number). login_digest is a keyed hash, so a login can be looked up and kept unique
-- per institution without decrypting every row.
create table academic_accounts
(
    id                  bigint generated always as identity primary key,
    user_id             bigint      not null references users (id) on delete cascade,
    institution_id      bigint      not null references institutions (id),
    login_ciphertext    text        not null,
    login_digest        bytea       not null,
    password_ciphertext text        not null,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now(),
    unique (user_id, institution_id),
    unique (institution_id, login_digest)
);

-- Catalog scraped from each institution, so names and terms are unique per institution

create table courses
(
    id             bigint generated always as identity primary key,
    institution_id bigint not null references institutions (id),
    name           text   not null,
    unique (institution_id, name)
);

create table academic_terms
(
    id             bigint generated always as identity primary key,
    institution_id bigint  not null references institutions (id),
    year           integer not null check (year > 1900),
    term           integer not null check (term > 0),
    unique (institution_id, year, term)
);

create table subjects
(
    id             bigint generated always as identity primary key,
    institution_id bigint not null references institutions (id),
    name           text   not null,
    unique (institution_id, name)
);

create table class_sections
(
    id               bigint generated always as identity primary key,
    academic_term_id bigint not null references academic_terms (id),
    course_id        bigint not null references courses (id),
    code             text   not null,
    unique (academic_term_id, course_id, code)
);

create index class_sections_course_id_idx on class_sections (course_id);

create table subject_offerings
(
    id               bigint generated always as identity primary key,
    class_section_id bigint  not null references class_sections (id),
    subject_id       bigint  not null references subjects (id),
    course_load      integer not null check (course_load >= 0),
    unique (class_section_id, subject_id)
);

create index subject_offerings_subject_id_idx on subject_offerings (subject_id);

-- Per student

create table enrollments
(
    id                    bigint generated always as identity primary key,
    user_id               bigint       not null references users (id) on delete cascade,
    subject_offering_id   bigint       not null references subject_offerings (id),
    final_grade           numeric(5, 2) check (final_grade >= 0),
    absences              integer      not null default 0 check (absences >= 0),
    attendance_percentage numeric(5, 2) check (attendance_percentage between 0 and 100),
    result                text         not null,
    created_at            timestamptz  not null default now(),
    updated_at            timestamptz  not null default now(),
    unique (user_id, subject_offering_id)
);

create index enrollments_subject_offering_id_idx on enrollments (subject_offering_id);

create table notifications
(
    id         bigint generated always as identity primary key,
    user_id    bigint      not null references users (id) on delete cascade,
    message    text        not null,
    created_at timestamptz not null default now()
);

create index notifications_user_id_created_at_idx on notifications (user_id, created_at desc);
