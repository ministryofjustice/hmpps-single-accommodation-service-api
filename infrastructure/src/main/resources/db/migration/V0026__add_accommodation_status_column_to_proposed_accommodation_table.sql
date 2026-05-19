ALTER TABLE proposed_accommodation
    ADD COLUMN accommodation_status_id UUID;

ALTER TABLE proposed_accommodation
    ADD FOREIGN KEY (accommodation_status_id) REFERENCES accommodation_status(id);
