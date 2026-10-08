# Data Dictionary

Generated schema documentation for the HMPPS Single Accommodation Service API.

## Domains

- [Persistence dictionary (Markdown)](./persistence.md)
- [Persistence dictionary (CSV)](./persistence.csv)
- [Persistence ERD (Mermaid)](./erd-persistence.mermaid)

## Generation Inputs

- Kotlin entities: [../../infrastructure/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/infrastructure/persistence/entity](../../infrastructure/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/infrastructure/persistence/entity)
- Flyway migrations: [../../infrastructure/src/main/resources/db/migration](../../infrastructure/src/main/resources/db/migration)

## Notes

- This initial generation focuses on the persistence entity package.
- If new entity packages are added, include them in subsequent runs.
