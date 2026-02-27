CREATE TABLE duty_to_refer(
    id                          UUID NOT NULL,
    crn                         TEXT NOT NULL,
    local_authority_area_id     UUID NOT NULL,
    reference_number            TEXT,
    submission_date             DATE NOT NULL,
    outcome_status              VARCHAR(80),
    outcome_date                DATE,
    created_by_user_id          UUID,
    created_at                  TIMESTAMP WITH TIME ZONE DEFAULT now(),
    last_updated_by_user_id     UUID,
    last_updated_at             TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY(id)
);
