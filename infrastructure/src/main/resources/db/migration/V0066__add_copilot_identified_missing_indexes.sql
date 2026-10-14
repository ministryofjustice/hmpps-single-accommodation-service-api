-- This complements the archived filter by supporting fast lookup of the case row itself
-- when the app is already working from a filtered active-case set.
CREATE INDEX idx_sas_case_archived_id
    ON sas_case (archived, id);

-- Common identifier lookups are grouped by a specific case and identifier type, so this
-- supports the case/identifier-type filter used by association and latest-identifier queries.
CREATE INDEX idx_sas_case_identifier_case_id_identifier_type
    ON sas_case_identifier (case_id, identifier_type);

-- The app frequently needs the latest identifier per case (for current CRN/PRN), so this
-- supports the ordered lookup pattern by case_id + identifier_type + created_at DESC.
CREATE INDEX idx_sas_case_identifier_case_id_identifier_type_created_at
    ON sas_case_identifier (case_id, identifier_type, created_at DESC);

-- Exact CRN/PRN resolution is a frequent access pattern; the identifier_type + identifier
-- ordering supports efficient joins and direct lookup without scanning all identifier rows.
CREATE INDEX idx_sas_case_identifier_identifier_type_identifier
    ON sas_case_identifier (identifier_type, identifier);
