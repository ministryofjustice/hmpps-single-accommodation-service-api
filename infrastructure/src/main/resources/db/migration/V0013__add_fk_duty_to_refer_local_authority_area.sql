ALTER TABLE duty_to_refer
    ADD CONSTRAINT fk_duty_to_refer_local_authority_area
    FOREIGN KEY (local_authority_area_id) REFERENCES local_authority_area(id);
