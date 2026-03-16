ALTER TABLE sas_case
    ADD COLUMN current_accommodation_arrangement_type TEXT;
ALTER TABLE sas_case
    ADD COLUMN next_accommodation_id UUID;
ALTER TABLE sas_case
    ADD COLUMN crs_status TEXT;
ALTER TABLE sas_case
    ADD COLUMN dtr_status TEXT;
ALTER TABLE sas_case
    ADD COLUMN cas3_application_placement_status TEXT;
ALTER TABLE sas_case
    ADD COLUMN cas3_application_application_status TEXT;
ALTER TABLE sas_case
    ADD COLUMN cas3_application_id UUID;
ALTER TABLE sas_case
    ADD COLUMN cas2_hdc_application_id UUID;
ALTER TABLE sas_case
    ADD COLUMN cas2_prison_bail_application_id UUID;
ALTER TABLE sas_case
    ADD COLUMN cas2_court_bail_application_id UUID;
ALTER TABLE sas_case
    ADD COLUMN cas1_application_placement_status TEXT;
ALTER TABLE sas_case
    ADD COLUMN cas1_application_application_status TEXT;
ALTER TABLE sas_case
    ADD COLUMN cas1_application_id UUID;
