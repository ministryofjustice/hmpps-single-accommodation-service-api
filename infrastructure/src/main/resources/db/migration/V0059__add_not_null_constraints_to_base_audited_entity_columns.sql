ALTER TABLE proposed_accommodation
    ALTER COLUMN created_by_user_id SET NOT NULL,
    ALTER COLUMN last_updated_by_user_id SET NOT NULL,
    ALTER COLUMN last_updated_at SET NOT NULL;

ALTER TABLE duty_to_refer
    ALTER COLUMN created_by_user_id SET NOT NULL,
    ALTER COLUMN last_updated_by_user_id SET NOT NULL,
    ALTER COLUMN last_updated_at SET NOT NULL;

ALTER TABLE external_referral
    ALTER COLUMN created_by_user_id SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN last_updated_by_user_id SET NOT NULL,
    ALTER COLUMN last_updated_at SET NOT NULL;
