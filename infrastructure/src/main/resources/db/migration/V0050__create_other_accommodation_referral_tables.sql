CREATE TABLE other_accommodation_referral (
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

CREATE INDEX idx_other_accommodation_referral_case_id ON other_accommodation_referral (case_id);

CREATE TABLE other_accommodation_referral_note (
                                             id                          UUID NOT NULL,
                                             other_accommodation_referral_id   UUID NOT NULL,
                                             note                        TEXT NOT NULL,
                                             created_by_user_id          UUID NOT NULL,
                                             created_at                  TIMESTAMP WITH TIME ZONE NOT NULL,
                                             last_updated_by_user_id     UUID NOT NULL,
                                             last_updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,
                                             PRIMARY KEY (id),
                                             FOREIGN KEY (other_accommodation_referral_id) REFERENCES other_accommodation_referral (id)
);

CREATE INDEX idx_other_accommodation_referral_not_accommodation_referral_id ON other_accommodation_referral_note (other_accommodation_referral_id);