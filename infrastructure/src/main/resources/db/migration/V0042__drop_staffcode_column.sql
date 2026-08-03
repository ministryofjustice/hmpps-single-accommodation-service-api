drop index if exists sas_user_delius_staff_code_idx;
alter table sas_user
    drop column delius_staff_code;