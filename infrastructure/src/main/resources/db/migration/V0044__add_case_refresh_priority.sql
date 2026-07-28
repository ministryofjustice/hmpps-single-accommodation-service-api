ALTER TABLE case_refresh_request
    ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'LIVE',
    ADD CONSTRAINT chk_case_refresh_request_priority
        CHECK (priority IN ('LIVE', 'BULK'));
