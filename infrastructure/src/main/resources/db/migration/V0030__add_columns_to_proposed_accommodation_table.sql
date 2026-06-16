ALTER TABLE proposed_accommodation
    ADD COLUMN accommodation_source VARCHAR(100) NOT NULL default 'SAS';

ALTER TABLE proposed_accommodation
    ADD COLUMN type_verified BOOLEAN;

ALTER TABLE proposed_accommodation
    ADD COLUMN no_fixed_abode BOOLEAN;
