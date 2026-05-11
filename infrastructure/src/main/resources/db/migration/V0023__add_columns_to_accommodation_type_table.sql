DROP TABLE IF EXISTS accommodation_type CASCADE;

CREATE TABLE accommodation_type
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    type_code    TEXT         NOT NULL,
    name         TEXT         NOT NULL,
    settled_type VARCHAR(100) NOT NULL,
    active       BOOLEAN      NOT NULL,
    is_proposed  BOOLEAN      NOT NULL,
    is_private   BOOLEAN      NOT NULL,
    is_prison    BOOLEAN      NOT NULL,
    is_cas1      BOOLEAN      NOT NULL,
    is_cas2      BOOLEAN      NOT NULL,


    PRIMARY KEY (id),
    CONSTRAINT unique_accommodation_type_name UNIQUE (name),
    CONSTRAINT unique_accommodation_type_type_code UNIQUE (type_code)
);

CREATE INDEX idx_accommodation_type_active ON accommodation_type (active);

INSERT INTO accommodation_type (type_code, name, settled_type, active, is_proposed, is_private,
                                is_prison, is_cas1, is_cas2)
VALUES ('A10', 'CAS2 accommodation of 13 weeks or more', 'TRANSIENT', true, true, false, false, false, true),
       ('A11', 'CAS2 accommodation of less than 13 weeks', 'TRANSIENT', true, true, false, false, false, true),
       ('A07B', 'Living in the home of a friend, family member or partner: settled', 'SETTLED', true, true, true,
        false, false, false),
       ('A07A', 'Living in the home of a friend, family member or partner: transient', 'TRANSIENT', true, true, true,
        false, false, false),
       ('A14', 'HOIE Section 10: staying in a Home Office specified residence', 'TRANSIENT', true, true, false, false,
        false, false),
       ('A13', 'HOIE Section 4: staying in Home Office accommodation support', 'TRANSIENT', true, true, false, false,
        false, false),
       ('A01A', 'Owner of the property', 'SETTLED', true, true, true, false, false, false),
       ('A15', 'Immigration Detention', 'TRANSIENT', true, true, false, false, false, false),
       ('A12', 'Living in a care home or nursing home', 'SETTLED', true, true, false, false, false, false),
       ('A01C', 'Renting from a private landlord or letting agent', 'SETTLED', true, true, true, false, false, false),
       ('A01D', 'Renting from a council or housing association (social housing)', 'SETTLED', true, true, true, false,
        false, false),
       ('A04', 'Supported housing (with support services)', 'SETTLED', true, true, false, false, false, false),
       ('A03', 'Transient or short-term accommodation', 'TRANSIENT', true, true, false, false, false, false),
       ('A02', 'Approved Premises', 'TRANSIENT', true, false, false, false, true, false),
       ('A16', 'Awaiting Assessment', 'TRANSIENT', true, false, false, false, false, false),
       ('A17', 'CAS3', 'TRANSIENT', true, false, false, false, false, false),
       ('A08A', 'Homeless - Rough Sleeping', 'TRANSIENT', true, false, false, false, false, false),
       ('A08C', 'Homeless - Shelter/Emergency Hostel/Campsite', 'TRANSIENT', true, false, false, false, false, false),
       ('A08', 'Homeless - Squat', 'TRANSIENT', true, false, false, false, false, false),
       ('HMP', 'Prison', 'TRANSIENT', true, false, false, true, false, false);
