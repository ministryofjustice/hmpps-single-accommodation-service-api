CREATE TABLE other_accommodation_referral(
      id                          UUID NOT NULL,
      crn                         TEXT NOT NULL,
      local_authority_area_id     UUID NOT NULL,
      reference_number            TEXT,
      submission_date             DATE NOT NULL,
      status                      VARCHAR(80) NOT NULL,
      case_id                     UUID NOT NULL,
      organisation_name           TEXT,
      website                     TEXT,
      submission_note             TEXT,
      created_by_user_id          UUID,
      created_at                  TIMESTAMP WITH TIME ZONE DEFAULT now(),
      last_updated_by_user_id     UUID,
      last_updated_at             TIMESTAMP WITH TIME ZONE,
      PRIMARY KEY(id),
      CONSTRAINT fk_other_accommodation_referral_local_authority_area
      FOREIGN KEY (local_authority_area_id) REFERENCES local_authority_area(id),
      CONSTRAINT fk_other_accommodation_referral_sas_case
      FOREIGN KEY (case_id) REFERENCES sas_case (id)
);
