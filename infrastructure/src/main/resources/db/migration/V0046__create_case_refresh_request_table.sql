CREATE TABLE sas_case_refresh_request
(
    case_id               UUID PRIMARY KEY REFERENCES sas_case (id) ON DELETE CASCADE,
    generation            BIGINT                   NOT NULL DEFAULT 1,
    processing_generation BIGINT,
    status                VARCHAR(20)              NOT NULL,
    priority              VARCHAR(10)              NOT NULL,
    requested_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    claimed_at            TIMESTAMP WITH TIME ZONE,
    claim_id              UUID,
    attempt_count         INTEGER                  NOT NULL DEFAULT 0,
    next_attempt_at       TIMESTAMP WITH TIME ZONE,
    last_failure_category VARCHAR(40),
    last_failure_detail   TEXT,
    failed_at             TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_sas_case_refresh_request_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'FAILED'))
);

CREATE INDEX idx_sas_case_refresh_request_pending
    ON sas_case_refresh_request (next_attempt_at, requested_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_sas_case_refresh_request_abandoned
    ON sas_case_refresh_request (claimed_at)
    WHERE status = 'PROCESSING';
