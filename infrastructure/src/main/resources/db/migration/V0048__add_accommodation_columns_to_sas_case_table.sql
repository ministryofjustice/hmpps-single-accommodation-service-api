ALTER TABLE sas_case
  ADD COLUMN current_accommodation jsonb,
  ADD COLUMN next_accommodation jsonb,
  ADD COLUMN accommodation_status varchar(50);
