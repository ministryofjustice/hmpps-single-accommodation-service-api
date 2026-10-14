ALTER TABLE sas_case
    ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_sas_case_archived
    ON sas_case (archived);
