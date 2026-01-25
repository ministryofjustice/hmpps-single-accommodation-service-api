CREATE TABLE sas_user(
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

CREATE UNIQUE INDEX sas_user_username_key ON sas_user (username);

CREATE INDEX sas_user_delius_staff_code_idx ON sas_user (delius_staff_code);