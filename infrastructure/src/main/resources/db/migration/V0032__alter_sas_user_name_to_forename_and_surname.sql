alter table sas_user
    add column forename      text,
    add column middle_names  text,
    add column surname       text;

-- forename and surname are non-nullable in the StaffDetail, so will be present.
UPDATE sas_user
SET forename = split_part(name, ' ', 1),
    surname  = CASE
                   WHEN position(' ' IN name) > 0
                       THEN substring(name FROM position(' ' IN name) + 1)
        END;

alter table sas_user
    alter column forename set not null,
    alter column surname set not null,
    drop column name;