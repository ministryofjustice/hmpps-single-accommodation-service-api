ALTER TABLE proposed_accommodation DROP COLUMN status;
ALTER TABLE proposed_accommodation ADD COLUMN verification_status VARCHAR(80);
ALTER TABLE proposed_accommodation ADD COLUMN next_accommodation_status VARCHAR(80);