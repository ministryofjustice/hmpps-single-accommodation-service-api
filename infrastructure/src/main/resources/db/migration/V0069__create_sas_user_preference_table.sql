CREATE TABLE sas_user_preference
(
    id              UUID                        NOT NULL PRIMARY KEY,
    sas_user_id     UUID                        NOT NULL REFERENCES sas_user (id),
    name            TEXT                        NOT NULL,
    value           TEXT                        NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_sas_user_preference_sas_user_id_name ON sas_user_preference (sas_user_id, name);
