ALTER TABLE sas_case_identifier
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;

-- add constraint so that only one CRN and one PRISON_NUMBER can be 'archived=false' at a time.
CREATE UNIQUE INDEX idx_case_identifier_case_id_identifier_type_active
    ON sas_case_identifier (case_id, identifier_type)
    WHERE archived = false
      AND identifier_type IN ('CRN', 'PRISON_NUMBER');
