alter table accommodation_type
    add column is_homeless boolean not null default false;
update accommodation_type a
set is_homeless = true
where a.type_code in ('A08A', 'A08C', 'A08');