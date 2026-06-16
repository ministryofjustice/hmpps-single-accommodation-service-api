INSERT INTO sas_user (id,username,auth_source,forename,middle_names,surname,email,telephone_number,delius_staff_code,nomis_staff_id,nomis_account_type,nomis_active_caseload_id,is_enabled,is_active)
VALUES (
gen_random_uuid(),
   'DELIUS_SYNC_USER',
   'DELIUS',
   'nDelius',
    null,
    'user',
   null,
   null,
   null,
   null,
   null,
   null,
   true,
   true
);
