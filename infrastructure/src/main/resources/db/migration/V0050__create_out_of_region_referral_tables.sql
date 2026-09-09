CREATE TABLE out_of_region_referral (
                                        id                          UUID NOT NULL,
                                        case_id                     UUID NOT NULL REFERENCES sas_case (id),
                                        reference_number            TEXT,
                                        submission_date             DATE NOT NULL,
                                        status                      VARCHAR(80) NOT NULL,
                                        outcome_reason              VARCHAR(80),
                                        submission_note             TEXT,
                                        outcome_note                TEXT,
                                        created_by_user_id          UUID REFERENCES sas_user (id),
                                        created_at                  TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
                                        last_updated_by_user_id     UUID REFERENCES sas_user (id),
                                        last_updated_at             TIMESTAMP WITH TIME ZONE,
                                        PRIMARY KEY (id)
);

CREATE INDEX idx_out_of_region_referral_case_id ON out_of_region_referral (case_id);

CREATE TABLE out_of_region_referral_note (
                                             id                          UUID NOT NULL,
                                             out_of_region_referral_id   UUID NOT NULL,
                                             note                        TEXT NOT NULL,
                                             created_by_user_id          UUID NOT NULL,
                                             created_at                  TIMESTAMP WITH TIME ZONE NOT NULL,
                                             last_updated_by_user_id     UUID NOT NULL,
                                             last_updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,
                                             PRIMARY KEY (id),
                                             FOREIGN KEY (out_of_region_referral_id) REFERENCES out_of_region_referral (id)
);

CREATE INDEX idx_out_of_region_referral_note_out_of_region_referral_id ON out_of_region_referral_note (out_of_region_referral_id);