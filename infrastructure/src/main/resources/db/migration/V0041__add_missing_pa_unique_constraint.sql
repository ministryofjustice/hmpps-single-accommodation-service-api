ALTER TABLE proposed_accommodation
    ADD CONSTRAINT unique_proposed_accommodation_case_id_cpr_address_id UNIQUE (case_id, cpr_address_id);
