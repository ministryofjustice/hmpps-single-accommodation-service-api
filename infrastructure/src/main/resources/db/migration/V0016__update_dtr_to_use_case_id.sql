alter table duty_to_refer
add column case_id UUID REFERENCES sas_case (id),
drop column crn
