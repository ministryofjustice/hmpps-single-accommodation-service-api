CREATE TABLE outbox_event (
    id UUID,
    aggregate_id UUID not null,
    aggregate_type TEXT not null,
    domain_event_type TEXT not null,
    payload TEXT not null,
    created_at TIMESTAMP WITH TIME ZONE default now(),
    processed_status VARCHAR(20) NOT NULL default 'PENDING',
    processed_at TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY(id)
);
