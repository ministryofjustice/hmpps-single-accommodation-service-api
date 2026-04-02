alter table proposed_accommodation
add column case_id UUID REFERENCES sas_case (id),
drop column crn
