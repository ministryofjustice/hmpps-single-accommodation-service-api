create table sas_case_identifier
(
    id uuid not null PRIMARY KEY,
    case_id uuid not null REFERENCES sas_case(id),
    identifier text not null,
    identifier_type text not null,
    created_at timestamptz not null default now()
);

create unique index idx_unique_identifier
    on sas_case_identifier(identifier, identifier_type);

drop index idx_sas_case_crn;
alter table sas_case drop column crn;

alter table sas_case rename column tier to tier_score;