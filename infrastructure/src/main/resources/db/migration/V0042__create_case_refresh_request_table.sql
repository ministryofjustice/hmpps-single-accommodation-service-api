CREATE TABLE case_refresh_request
(
    case_id               UUID PRIMARY KEY REFERENCES sas_case (id) ON DELETE CASCADE,
    generation            BIGINT                   NOT NULL DEFAULT 1,
    processing_generation BIGINT,
    status                VARCHAR(20)              NOT NULL
        CHECK (status IN ('PENDING', 'PROCESSING')),
    requested_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    claimed_at            TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_case_refresh_request_status_requested_at
    ON case_refresh_request (status, requested_at);