alter table sas_case_identifier alter column case_id set not null;
alter table proposed_accommodation alter column case_id set not null;
alter table duty_to_refer alter column case_id set not null;

create index idx_duty_to_refer_case_id on duty_to_refer(case_id);
create index idx_duty_to_refer_local_authority_area_id on duty_to_refer(local_authority_area_id);
create index idx_sas_case_identifier_case_id on sas_case_identifier(case_id);
create index idx_proposed_accommodation_case_id on proposed_accommodation(case_id);