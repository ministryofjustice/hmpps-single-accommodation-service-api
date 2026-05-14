CREATE TABLE accommodation_status
(
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    status_code     TEXT         NOT NULL,
    name            TEXT         NOT NULL,
    active          BOOLEAN      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT unique_accommodation_status_name UNIQUE (name),
    CONSTRAINT unique_accommodation_status_code UNIQUE (status_code)
);

CREATE INDEX idx_accommodation_status_active ON accommodation_status (active);

INSERT INTO accommodation_status (status_code, name, active)
VALUES ('B', 'Bail', true),
       ('M', 'Main', true),
       ('MA', 'Postal', true),
       ('P', 'Previous', true),
       ('PR', 'Proposed', true),
       ('PR1', 'Proposed for Resettlement', true),
       ('RJ', 'Rejected', true),
       ('RT', 'ROTL', true),
       ('S', 'Secondary', true);
