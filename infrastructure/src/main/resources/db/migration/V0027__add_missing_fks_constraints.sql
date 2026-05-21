-- proposed_accommodation
alter table proposed_accommodation
    alter column created_at set not null,
    add constraint proposed_accommodation_created_by_user_fkey
        foreign key (created_by_user_id)
            references sas_user(id),
    add constraint proposed_accommodation_last_updated_by_user_fkey
        foreign key (last_updated_by_user_id)
            references sas_user(id);

-- duty_to_refer
alter table duty_to_refer
    alter column created_at set not null,
    add constraint duty_to_refer_created_by_user_fkey
        foreign key (created_by_user_id)
            references sas_user(id),
    add constraint duty_to_refer_last_updated_by_user_fkey
        foreign key (last_updated_by_user_id)
            references sas_user(id);

-- outbox_event
create index idx_outbox_event_processed_status
    on outbox_event(processed_status);

alter table outbox_event
    alter column created_at set not null;

-- inbox_event
create index idx_inbox_event_processed_status
    on inbox_event(processed_status);

alter table inbox_event
    alter column created_at set not null;

-- sas_user
alter table sas_user
    alter column created_at set not null,
    alter column is_active set not null,
    alter column created_at set not null;

drop index if exists sas_user_username_key; -- named as key but is index. also could conflict if a user has delius and nomis accounts.
create unique index idx_sas_user_username_auth_source on sas_user (username, auth_source);

-- local_authority_area
alter table local_authority_area
    alter column active set not null;

