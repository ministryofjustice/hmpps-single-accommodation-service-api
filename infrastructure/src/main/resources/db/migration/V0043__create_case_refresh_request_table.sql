CREATE TABLE case_refresh_request
(
    case_id               UUID PRIMARY KEY REFERENCES sas_case (id) ON DELETE CASCADE,
    generation            BIGINT                   NOT NULL DEFAULT 1,
    processing_generation BIGINT,
    status                VARCHAR(20)              NOT NULL,
    priority              VARCHAR(10)              NOT NULL DEFAULT 'LIVE',
    requested_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    claimed_at            TIMESTAMP WITH TIME ZONE,
    claim_id              UUID,
    attempt_count         INTEGER                  NOT NULL DEFAULT 0,
    next_attempt_at       TIMESTAMP WITH TIME ZONE,
    last_failure_category VARCHAR(40),
    last_failure_detail   TEXT,
    failed_at             TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_case_refresh_request_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'FAILED')),

    CONSTRAINT chk_case_refresh_request_priority
        CHECK (priority IN ('LIVE', 'BULK')),

    CONSTRAINT chk_case_refresh_request_failure_category
        CHECK (
            last_failure_category IS NULL
                OR last_failure_category IN (
                                             'CURRENT_TIER_NOT_FOUND',
                                             'UPSTREAM_CLIENT_ERROR',
                                             'UPSTREAM_SERVER_ERROR',
                                             'UPSTREAM_TIMEOUT',
                                             'UPSTREAM_UNEXPECTED_ERROR',
                                             'UNEXPECTED_ERROR'
                )
            ),

    CONSTRAINT chk_case_refresh_request_state
        CHECK (
            (
                status = 'PENDING'
                    AND next_attempt_at IS NOT NULL
                    AND processing_generation IS NULL
                    AND claimed_at IS NULL
                    AND claim_id IS NULL
                    AND failed_at IS NULL
                )
                OR (
                status = 'PROCESSING'
                    AND processing_generation IS NOT NULL
                    AND claimed_at IS NOT NULL
                    AND claim_id IS NOT NULL
                    AND failed_at IS NULL
                )
                OR (
                status = 'FAILED'
                    AND next_attempt_at IS NULL
                    AND processing_generation IS NULL
                    AND claimed_at IS NULL
                    AND claim_id IS NULL
                    AND failed_at IS NOT NULL
                )
            )
);

CREATE INDEX idx_case_refresh_request_pending
    ON case_refresh_request (next_attempt_at, requested_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_case_refresh_request_abandoned
    ON case_refresh_request (claimed_at)
    WHERE status = 'PROCESSING';
