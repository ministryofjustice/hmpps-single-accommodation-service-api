ALTER TABLE proposed_accommodation DROP COLUMN arrangement_type;
ALTER TABLE proposed_accommodation DROP COLUMN arrangement_sub_type;
ALTER TABLE proposed_accommodation DROP COLUMN arrangement_sub_type_description;
ALTER TABLE proposed_accommodation DROP COLUMN settled_type;
ALTER TABLE proposed_accommodation DROP COLUMN offender_release_type;

ALTER TABLE proposed_accommodation
    ADD COLUMN accommodation_type_id UUID;

ALTER TABLE proposed_accommodation
    ADD FOREIGN KEY (accommodation_type_id) REFERENCES accommodation_type(id)