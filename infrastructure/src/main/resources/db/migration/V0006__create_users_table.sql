CREATE TABLE users(
    id                          UUID not null,
    username                    TEXT not null,
    auth_source                 VARCHAR(80) not null,
    name                        TEXT not null,
    email                       TEXT not null default 'unknown@digital.justice.gov.uk'::text,
    telephone_number            TEXT,
    delius_staff_code           TEXT,
    nomis_staff_id              BIGINT,
    nomis_account_type          TEXT,
    nomis_active_caseload_id    TEXT,
    is_enabled                  BOOLEAN,
    is_active                   BOOLEAN not null,
    created_at                  TIMESTAMP WITH TIME ZONE default now(),
    PRIMARY KEY(id)
);

CREATE UNIQUE INDEX users_username_key on users (username);

CREATE INDEX users_delius_staff_code_idx
    on users (delius_staff_code);