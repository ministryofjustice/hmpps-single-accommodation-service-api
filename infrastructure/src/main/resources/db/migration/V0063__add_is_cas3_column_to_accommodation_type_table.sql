ALTER TABLE accommodation_type
    ADD COLUMN is_cas3 BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE accommodation_type
SET is_cas3 = TRUE
WHERE type_code = 'A17';
