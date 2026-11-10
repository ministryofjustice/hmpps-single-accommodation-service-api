-- Add the FK constraints to sas_user
ALTER TABLE external_referral
    ADD CONSTRAINT external_referral_created_by_user_fkey
        FOREIGN KEY (created_by_user_id)
            REFERENCES sas_user (id),
    ADD CONSTRAINT external_referral_last_updated_by_user_fkey
        FOREIGN KEY (last_updated_by_user_id)
            REFERENCES sas_user (id);