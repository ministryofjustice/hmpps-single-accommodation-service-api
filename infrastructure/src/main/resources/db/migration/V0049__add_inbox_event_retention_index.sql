CREATE INDEX idx_inbox_event_processed_status_processed_at
    ON inbox_event (processed_status, processed_at);
