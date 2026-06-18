# Data Dictionary: Persistence Domain

## Scope

This dictionary documents physical persistence entities under:

- `infrastructure/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/infrastructure/persistence/entity`
- `infrastructure/src/main/resources/db/migration`

It is generated from Kotlin JPA entities and Flyway SQL migrations.

## Entity Relationship Diagram

- [erd-persistence.mermaid](./erd-persistence.mermaid)

## Tables

### CaseEntity (`sas_case`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| tier_score | TEXT | String | Yes |  |  | Renamed from `tier` in V0014 |
| cas1_application_placement_status | TEXT | Cas1PlacementStatus | Yes |  | `ARRIVED`, `UPCOMING`, `DEPARTED`, `NOT_ARRIVED`, `CANCELLED` | Stored as string |
| cas1_application_request_for_placement_status | TEXT | Cas1RequestForPlacementStatus | Yes |  | `REQUEST_UNSUBMITTED`, `REQUEST_REJECTED`, `REQUEST_SUBMITTED`, `AWAITING_MATCH`, `REQUEST_WITHDRAWN`, `PLACEMENT_BOOKED` | Stored as string |
| cas1_application_application_status | TEXT | Cas1ApplicationStatus | Yes |  | `AWAITING_ASSESSMENT`, `UNALLOCATED_ASSESSMENT`, `ASSESSMENT_IN_PROGRESS`, `AWAITING_PLACEMENT`, `PLACEMENT_ALLOCATED`, `REQUEST_FOR_FURTHER_INFORMATION`, `PENDING_PLACEMENT_REQUEST`, `STARTED`, `REJECTED`, `INAPPLICABLE`, `WITHDRAWN`, `EXPIRED` | Stored as string |
| cas1_application_id | UUID | UUID | Yes |  |  |  |

### CaseIdentifierEntity (`sas_case_identifier`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| case_id | UUID | UUID | No | FK -> `sas_case.id` |  |  |
| identifier | TEXT | String | No |  |  |  |
| identifier_type | TEXT | IdentifierType | No | unique with `identifier` | `CRN`, `PRISON_NUMBER` |  |
| created_at | TIMESTAMPTZ | Instant | No |  |  | default `now()` |

### DutyToReferEntity (`duty_to_refer`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| case_id | UUID | UUID | No | FK -> `sas_case.id` |  |  |
| local_authority_area_id | UUID | UUID | No | FK -> `local_authority_area.id` |  |  |
| reference_number | TEXT | String | Yes |  |  |  |
| submission_date | DATE | LocalDate | No |  |  |  |
| status | VARCHAR(80) | DtrStatus | No |  | `SUBMITTED`, `ACCEPTED`, `NOT_ACCEPTED`, `WITHDRAWN` |  |
| withdrawal_reason | VARCHAR(80) | WithdrawalReason | Yes |  | `NEW_REFERRAL`, `INCORRECT_LOCAL_AUTHORITY`, `NO_CONSENT`, `DISENGAGED`, `HOUSING_NEED_RESOLVED`, `NOT_ELIGIBLE`, `OTHER` |  |
| withdrawal_reason_other | TEXT | String | Yes |  |  |  |
| outcome_reason | VARCHAR(80) | OutcomeReason | Yes |  | `PREVENTION_AND_RELIEF_DUTY`, `PRIORITY_NEED`, `NO_LOCAL_CONNECTION`, `INTENTIONALLY_HOMELESS`, `REJECTED_FOR_ANOTHER_REASON` |  |
| submission_note | TEXT | String | Yes |  |  |  |
| outcome_note | TEXT | String | Yes |  |  |  |
| created_by_user_id | UUID | UUID | Yes | FK -> `sas_user.id` |  | inherited from `BaseAuditedEntity` |
| created_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable; migration sets NOT NULL/default `now()` |
| last_updated_by_user_id | UUID | UUID | Yes | FK -> `sas_user.id` |  | inherited from `BaseAuditedEntity` |
| last_updated_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity` |

### DutyToReferNoteEntity (`duty_to_refer_note`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| duty_to_refer_id | UUID | UUID | No | FK -> `duty_to_refer.id` |  |  |
| note | TEXT | String | No |  |  |  |
| created_by_user_id | UUID | UUID | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable; migration sets NOT NULL |
| created_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable; migration sets NOT NULL |
| last_updated_by_user_id | UUID | UUID | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable; migration sets NOT NULL |
| last_updated_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable; migration sets NOT NULL |

### ProposedAccommodationEntity (`proposed_accommodation`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| case_id | UUID | UUID | No | FK -> `sas_case.id` |  |  |
| cpr_address_id | UUID | UUID | Yes |  |  |  |
| name | VARCHAR(200) | String | Yes |  |  |  |
| accommodation_type_id | UUID | UUID | No | FK -> `accommodation_type.id` |  |  |
| accommodation_status_id | UUID | UUID | Yes | FK -> `accommodation_status.id` |  |  |
| verification_status | VARCHAR(80) | VerificationStatus | Yes |  | `NOT_CHECKED_YET`, `FAILED`, `PASSED` |  |
| next_accommodation_status | VARCHAR(80) | NextAccommodationStatus | Yes |  | `YES`, `NO`, `TO_BE_DECIDED` |  |
| start_date | DATE | LocalDate | Yes |  |  |  |
| end_date | DATE | LocalDate | Yes |  |  |  |
| postcode | TEXT | String | Yes |  |  |  |
| sub_building_name | TEXT | String | Yes |  |  |  |
| building_name | TEXT | String | Yes |  |  |  |
| building_number | TEXT | String | Yes |  |  |  |
| throughfare_name | TEXT | String | Yes |  |  |  |
| dependent_locality | TEXT | String | Yes |  |  |  |
| post_town | TEXT | String | Yes |  |  |  |
| county | TEXT | String | Yes |  |  |  |
| country | TEXT | String | Yes |  |  |  |
| uprn | TEXT | String | Yes |  |  |  |
| accommodation_source | VARCHAR(100) | AccommodationSource | No |  | `DELIUS`, `SAS` | default `SAS` |
| no_fixed_abode | BOOLEAN | Boolean | Yes |  |  |  |
| type_verified | BOOLEAN | Boolean | Yes |  |  |  |
| created_by_user_id | UUID | UUID | Yes | FK -> `sas_user.id` |  | inherited from `BaseAuditedEntity` |
| created_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable; migration sets NOT NULL/default `now()` |
| last_updated_by_user_id | UUID | UUID | Yes | FK -> `sas_user.id` |  | inherited from `BaseAuditedEntity` |
| last_updated_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity` |

### ProposedAccommodationNoteEntity (`proposed_accommodation_note`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| proposed_accommodation_id | UUID | UUID | No | FK -> `proposed_accommodation.id` |  |  |
| note | TEXT | String | No |  |  |  |
| created_by_user_id | UUID | UUID | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable |
| created_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable |
| last_updated_by_user_id | UUID | UUID | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable |
| last_updated_at | TIMESTAMPTZ | Instant | Yes |  |  | inherited from `BaseAuditedEntity`; Kotlin property is nullable |

### AccommodationTypeEntity (`accommodation_type`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  | default `gen_random_uuid()` |
| type_code | TEXT | String | No | UNIQUE |  |  |
| name | TEXT | String | No | UNIQUE |  |  |
| settled_type | VARCHAR(100) | AccommodationSettledType | No |  | `SETTLED`, `TRANSIENT` |  |
| active | BOOLEAN | Boolean | No |  |  |  |
| is_proposed | BOOLEAN | Boolean | No |  |  |  |
| is_private | BOOLEAN | Boolean | No |  |  |  |
| is_prison | BOOLEAN | Boolean | No |  |  |  |
| is_cas1 | BOOLEAN | Boolean | No |  |  |  |
| is_cas2 | BOOLEAN | Boolean | No |  |  |  |

### AccommodationStatusEntity (`accommodation_status`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  | default `gen_random_uuid()` |
| status_code | TEXT | String | No | UNIQUE |  |  |
| name | TEXT | String | No | UNIQUE |  |  |
| active | BOOLEAN | Boolean | No |  |  |  |

### LocalAuthorityAreaEntity (`local_authority_area`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  | default `gen_random_uuid()` |
| identifier | TEXT | String | No | UNIQUE |  |  |
| name | TEXT | String | No |  |  |  |
| active | BOOLEAN | Boolean | No |  |  | default `true` |

### OutboxEventEntity (`outbox_event`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| aggregate_id | UUID | UUID | No |  |  |  |
| aggregate_type | TEXT | String | No |  |  |  |
| domain_event_type | TEXT | String | No |  |  |  |
| payload | TEXT | String | No |  |  |  |
| created_at | TIMESTAMPTZ | Instant | No |  |  | default `now()` |
| processed_status | VARCHAR(20) | ProcessedStatus | No | index `idx_outbox_event_processed_status` | `PENDING`, `PROCESSED`, `NOT_PROCESSED`, `FAILED` |  |
| processed_at | TIMESTAMPTZ | Instant | Yes |  |  |  |

### InboxEventEntity (`inbox_event`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| event_type | TEXT | String | No |  |  |  |
| event_detail_url | TEXT | String | Yes |  |  | Migration defines NOT NULL, Kotlin field is nullable |
| event_occurred_at | TIMESTAMPTZ | OffsetDateTime | No | index with processed status |  |  |
| created_at | TIMESTAMPTZ | Instant | No |  |  | default `now()` |
| processed_status | VARCHAR(20) | ProcessedStatus | No | index `idx_inbox_event_processed_status` | `PENDING`, `PROCESSED`, `NOT_PROCESSED`, `FAILED` |  |
| processed_at | TIMESTAMPTZ | Instant | Yes |  |  |  |
| payload | JSONB | String | No |  |  | JSON string stored in JSONB column |

### UserEntity (`sas_user`)

| Column | SQL Type | Kotlin Type | Nullable | Key / Constraint | Enum Values | Notes |
|---|---|---|---|---|---|---|
| id | UUID | UUID | No | PK |  |  |
| username | TEXT | String | No | UNIQUE with `auth_source` |  |  |
| auth_source | VARCHAR(80) | AuthSource | No | UNIQUE with `username` | `NOMIS`, `DELIUS` |  |
| forename | TEXT | String | No |  |  | Added in V0032 |
| middle_names | TEXT | String | Yes |  |  | Added in V0032 |
| surname | TEXT | String | No |  |  | Added in V0032 |
| email | TEXT | String | Yes |  |  |  |
| telephone_number | TEXT | String | Yes |  |  |  |
| delius_staff_code | TEXT | String | Yes | index `sas_user_delius_staff_code_idx` |  |  |
| nomis_staff_id | BIGINT | Long | Yes |  |  |  |
| nomis_account_type | TEXT | String | Yes |  |  |  |
| nomis_active_caseload_id | TEXT | String | Yes |  |  |  |
| is_enabled | BOOLEAN | Boolean | Yes |  |  |  |
| is_active | BOOLEAN | Boolean | No |  |  |  |
| created_at | TIMESTAMPTZ | Instant | No |  |  | default `now()` |

## Query-backed projections (not physical tables)

No query-backed projection entities were detected in the persistence entity package.

## Sources

| Type | Path |
|---|---|
| Entity package | [infrastructure/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/infrastructure/persistence/entity](../../infrastructure/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/infrastructure/persistence/entity) |
| Flyway migrations | [infrastructure/src/main/resources/db/migration](../../infrastructure/src/main/resources/db/migration) |
| CSV output | [docs/data-dictionary/persistence.csv](./persistence.csv) |
| ERD output | [docs/data-dictionary/erd-persistence.mermaid](./erd-persistence.mermaid) |