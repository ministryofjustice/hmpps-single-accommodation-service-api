CREATE TABLE accommodation_type(
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    delius_code     TEXT NOT NULL,
    name            TEXT NOT NULL,
    settled_type    VARCHAR(100) NOT NULL,
    active          BOOLEAN NOT NULL,
    PRIMARY KEY(id),
    CONSTRAINT unique_accommodation_type_name UNIQUE(name),
    CONSTRAINT unique_accommodation_type_delius_code UNIQUE(delius_code)
);

CREATE INDEX idx_accommodation_type_active ON accommodation_type(active);

INSERT INTO accommodation_type (delius_code, name, settled_type, active) VALUES
('A10', 'CAS2 accommodation of 13 weeks or more', 'TRANSIENT', true),
('A11', 'CAS2 accommodation of less than 13 weeks', 'TRANSIENT',true),
('A07B', 'Living in the home of a friend, family member or partner: settled','SETTLED', true),
('A07A', 'Living in the home of a friend, family member or partner: transient', 'TRANSIENT',true),
('A14', 'HOIE Section 10: staying in a Home Office specified residence', 'TRANSIENT',true),
('A13', 'HOIE Section 4: staying in Home Office accommodation support', 'TRANSIENT',true),
('A01A', 'Owner of the property', 'SETTLED', true),
('A15', 'Immigration Detention', 'TRANSIENT',true),
('A12', 'Living in a care home or nursing home', 'SETTLED', true),
('A01C', 'Renting from a private landlord or letting agent', 'SETTLED', true),
('A01D', 'Renting from a council or housing association (social housing)', 'SETTLED', true),
('A04', 'Supported housing (with support services)', 'SETTLED', true),
('A03', 'Transient or short-term accommodation', 'TRANSIENT',true);
