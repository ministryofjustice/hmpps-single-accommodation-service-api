CREATE INDEX idx_sas_case_crn ON sas_case (crn);

CREATE INDEX idx_inbox_event_processed_status_occurred_at
    ON inbox_event (processed_status, event_occurred_at);