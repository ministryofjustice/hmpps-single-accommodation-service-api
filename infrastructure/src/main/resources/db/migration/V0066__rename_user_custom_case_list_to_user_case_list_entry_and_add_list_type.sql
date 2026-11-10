ALTER TABLE sas_user_custom_case_list RENAME TO sas_user_case_list_entry;

ALTER INDEX sas_user_custom_case_list_pkey RENAME TO sas_user_case_list_entry_pkey;
ALTER TABLE sas_user_case_list_entry RENAME CONSTRAINT sas_user_custom_case_list_sas_user_id_fkey TO sas_user_case_list_entry_sas_user_id_fkey;
ALTER TABLE sas_user_case_list_entry RENAME CONSTRAINT sas_user_custom_case_list_sas_case_id_fkey TO sas_user_case_list_entry_sas_case_id_fkey;

ALTER TABLE sas_user_case_list_entry ADD COLUMN list_type VARCHAR(50) NOT NULL DEFAULT 'CUSTOM';
ALTER TABLE sas_user_case_list_entry ALTER COLUMN list_type DROP DEFAULT;
ALTER TABLE sas_user_case_list_entry
    ADD CONSTRAINT chk_sas_user_case_list_entry_list_type CHECK (list_type IN ('CUSTOM', 'SEARCH_HISTORY'));

DROP INDEX idx_sas_user_custom_case_list_sas_user_id_sas_case_id;
CREATE UNIQUE INDEX idx_sas_user_case_list_entry_sas_user_id_list_type_sas_case_id
    ON sas_user_case_list_entry (sas_user_id, list_type, sas_case_id);
