ALTER TABLE sas_user_case_list_entry ADD COLUMN last_updated_at TIMESTAMP WITH TIME ZONE;
UPDATE sas_user_case_list_entry SET last_updated_at = created_at;
ALTER TABLE sas_user_case_list_entry ALTER COLUMN last_updated_at SET NOT NULL;
ALTER TABLE sas_user_case_list_entry ALTER COLUMN last_updated_at SET DEFAULT now();
