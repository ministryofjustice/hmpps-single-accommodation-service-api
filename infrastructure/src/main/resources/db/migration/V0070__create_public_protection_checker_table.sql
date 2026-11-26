CREATE TABLE public_protection_checker(
  id                              UUID NOT NULL,
  sas_case_id                     UUID NOT NULL,
  created_by_user_id              UUID NOT NULL,
  created_at                      TIMESTAMP WITH TIME ZONE NOT NULL,
  supervision_required            BOOLEAN NOT NULL,
  control_measures_suitable       BOOLEAN NOT NULL,
  practical_support_sufficient    BOOLEAN NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT fk_public_protection_checker_sas_case
    FOREIGN KEY (sas_case_id) REFERENCES sas_case(id),
  CONSTRAINT fk_public_protection_checker_sas_user
    FOREIGN KEY (created_by_user_id) REFERENCES sas_user(id)
);

CREATE INDEX idx_public_protection_checker_sas_case_id ON public_protection_checker(sas_case_id);
