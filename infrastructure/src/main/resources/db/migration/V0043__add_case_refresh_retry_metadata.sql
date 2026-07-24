ALTER TABLE case_refresh_request
    DROP CONSTRAINT IF EXISTS case_refresh_request_status_check;

ALTER TABLE case_refresh_request
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN next_attempt_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN last_failure_category VARCHAR(40),
    ADD COLUMN last_failure_detail TEXT,
    ADD COLUMN failed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN claim_id UUID;

UPDATE case_refresh_request
SET next_attempt_at = requested_at
WHERE next_attempt_at IS NULL;

UPDATE case_refresh_request
SET status = 'PENDING',
    processing_generation = NULL,
    claimed_at = NULL,
    next_attempt_at = requested_at
WHERE status = 'PROCESSING';

ALTER TABLE case_refresh_request
    ADD CONSTRAINT chk_case_refresh_request_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'FAILED')),
    ADD CONSTRAINT chk_case_refresh_request_failure_category
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
    ADD CONSTRAINT chk_case_refresh_request_state
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
            );

CREATE INDEX idx_case_refresh_request_pending
    ON case_refresh_request (next_attempt_at, requested_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_case_refresh_request_abandoned
    ON case_refresh_request (claimed_at)
    WHERE status = 'PROCESSING';