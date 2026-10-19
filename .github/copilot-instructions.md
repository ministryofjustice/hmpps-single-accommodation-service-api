# Copilot instructions for `hmpps-single-accommodation-service-api`

## What this repo is
- Kotlin/Spring Boot backend for the **Single Accommodation Service (SAS)** frontend.
- Multi-module Gradle repo with a root Spring Boot app plus `common`, `infrastructure`, `query`, and `mutation` modules.
- Runtime stack: **Java 25**, **Gradle 9.7.1 wrapper**, Spring Boot, JPA/PostgreSQL, Redis/Redisson, LocalStack SNS/SQS, Flyway, SpringDoc, Detekt, ktlint.
- Size/signals: ~450 Kotlin files across modules; read/write concerns are intentionally split.

## Trust these instructions first
Use this file as the default operating guide. Only search the repo when these instructions are incomplete or proved wrong.

## Scope discipline
- Do **not** refactor code in the same area while making a requested change.
- Do **not** get sidetracked by warnings, recommendations, or cleanup opportunities unless the task explicitly asks for them.
- Do **not** manually format or reformat code as part of unrelated changes; rely on running `./gradlew ktlintFormat --no-daemon` when formatting is needed.
- Focus solely on the task you were asked to do, and keep changes as small and targeted as possible.

## Tooling safety
- For focused integration tests in this repo, use the root task explicitly: `./gradlew :integrationTest --tests '...' --no-daemon`.
- Never use heredocs directly in `run_in_terminal`. Use a temporary file instead.

## Avoid duplication
- Avoid introducing duplicated code when making changes; prefer existing helpers, fixtures, builders, and shared utilities where appropriate.
- In reviews, call out meaningful duplication and suggest consolidation, especially repeated test data setup, fixture construction, or repeated mapping logic.
- Keep any deduplication work scoped to the task at hand unless the request explicitly asks for broader cleanup.

## Fast repo map
- `src/main/kotlin/.../SingleAccommodationServiceApi.kt` — main entrypoint.
- `src/main/kotlin/.../api/controller/` — REST controllers.
- `src/main/resources/application.yml` — main app config.
- `src/main/resources/application-local.yml` — local profile.
- `src/test/resources/application-test.yml` — test profile; integration tests expect Postgres on `localhost:5434`, Redis on `6381`, LocalStack on `4566`.
- `common/` — shared DTOs, exceptions, test fixtures.
- `infrastructure/` — DB entities/repositories, Flyway migrations, external clients, security, caching, aggregator, app infrastructure config.
- `query/` — read-side services/orchestration/eligibility rules.
- `mutation/` — write-side application services, aggregates, inbox/outbox/event processing.
- `docs/architecture/decisions/` — ADRs describing DDD-lite, query/mutation split, and aggregation patterns.
- `scripts/` — Flyway helpers and Cloud Platform DB/Redis tunnel scripts.
- `helm_deploy/` — Helm chart values used by deployment workflows.

## Architecture facts that reduce bad edits
- Follow the established read path: **Controller -> Query/Application Service -> Orchestration Service -> `AggregatorService` / persistence**.
- Upstream HTTP aggregation lives in `infrastructure/.../aggregator/AggregatorService.kt`; prefer using existing caching/orchestration services rather than adding direct client calls in controllers.
- Write flows live in `mutation/` and usually go **Controller -> Mutation application service -> aggregate/repository -> inbox/outbox or event handlers**; avoid letting controllers reach straight into persistence.
- Dependency rules are enforced at build time via the Gradle project graph; violations typically surface as compile errors rather than failing tests:
  - the main app (`src/`) depends on `common`, `infrastructure`, `query`, and `mutation`
  - `query` depends on `common` + `infrastructure` only
  - `mutation` depends on `common` + `infrastructure` only
  - `infrastructure` depends on `common` only
  - `query` and `mutation` MUST NOT depend on each other
  - shared code belongs in `common` or `infrastructure`
- Controllers live in the app (`src/main/.../api/controller`) and delegate to query services for reads and/or mutation application services for writes.
- Persistence and migrations live under `infrastructure/src/main/resources/db/migration` and filenames are validated by `scripts/validate_flyway_filenames.sh` (`VNNNN__description.sql`).
- Kotlin formatting uses `.editorconfig`: **2-space indent** for `.kt`/`.kts`.

## Development standards
- Write clean, idiomatic Kotlin; prefer immutable data structures.
- Use data classes, sealed classes, extension functions, null safety, value classes, and coroutines where appropriate.
- Follow Spring Boot best practices; constructor injection by default (field injection acceptable in test-only contexts matching existing patterns).
- Keep business logic out of controllers; respect the layered flow above.
- Prefer configuration properties over scattered `@Value` usage, though `@Value` with defaults matches existing patterns when already established (for example, `@param:Value("${case-list.page-size:100}")`).
- Use Jakarta Bean Validation on controllers.
- Use global exception handling with `@ControllerAdvice` / `@RestControllerAdvice`.
- Use SLF4J logging via `LoggerFactory.getLogger(javaClass)`.
- Prefer profile-based config for `local` and `test`.

## Testing
- Unit tests are plain JVM tests: use JUnit 5, AssertJ, and MockK with factories/builders; avoid loading Spring unless a test explicitly needs it.
- Integration tests use the real Spring context and `IntegrationTestBase`, plus Postgres/Redis/LocalStack from `docker compose`.
- Keep tests deterministic and package-aligned with the code under test; prefer focused tests while iterating and the full suite before handoff for broad changes.
- For HTTP endpoints, cover the expected auth, happy path, validation/error path, and side effects/persistence changes.

## Subject Access Requests
- SAR functionality is part of this repo: the service aggregates data into a single SAR response and HTML report.
- The main SAR template is `src/main/resources/sar/template_hmpps-single-accommodation-service-api.mustache`.
- SAR compliance tests and fixture generation live under `src/test/kotlin/.../integration/sar/` with expected fixtures in `src/test/resources/sar/`.
- The SAR test fixture guide is `sar-test-fixtures-guide.md`; follow it when changing SAR data retrieval or the template.
- To regenerate fixtures, run the SAR integration tests with `SAR_GENERATE_ACTUAL=true` and use `:integrationTest` (not the unqualified task name) because integration tasks exist in the root and subprojects.
- Changes to SAR-relevant entities, schema, or Flyway migrations may require updating SAR fixtures and schema-related expectations alongside the code change.

```text
hmpps-single-accommodation-service-api/
├── src/                 # Main Spring Boot app: bootstrap, config, health, REST controllers (api/controller)
├── common/              # Shared DTOs, exceptions, small helpers (+ testFixtures: DTO factories)
├── infrastructure/      # JPA entities, repositories & persistence services, upstream API clients +
│                        #   caching services, AggregatorService, security, audit, sentry, config,
│                        #   Flyway migrations, messaging (SQS listener → inbox, outbox publisher),
│                        #   accommodation helpers (+ testFixtures: entity/client factories)
├── query/               # READ side: query services, *OrchestrationService, *OrchestrationDto,
│                        #   the eligibility rules engine (engine + rule domains + graph generator)
└── mutation/            # WRITE side: DDD-lite aggregates, application services, mappers,
                         #   inbox event dispatcher + handlers, admin/bulk jobs
```


## Validated toolchain and environment
Validated on macOS on **2026-10-12** with:
- `java -version` → Temurin/OpenJDK **25.0.1 LTS**
- `./gradlew --version` → **Gradle 9.7.1**
- `docker compose version` → **v5.0.0-desktop.1**

## Bootstrap / local dependencies
Always start Docker services before **integration tests** or **local app runs**:
```bash
# run from the repository root
docker compose up -d
```
Validated services/ports from `docker-compose.yml`:
- Postgres `localhost:5434`
- Redis `localhost:6381`
- LocalStack `localhost:4566`

To stop them:
```bash
docker compose down
```

## Validated command order
### 1) Kotlin validation / compile / lint without tests
Matches CI `kotlin_validate` job:
```bash
./gradlew clean check -x test --no-daemon
```
Validated: **success**, ~30s from clean state.

### 2) Lint / static analysis
README command works:
```bash
./gradlew ktlintFormat detekt --no-daemon
```
Validated: **success**, ~14s.

### 3) Unit tests
Matches CI `unit_test` job:
```bash
./gradlew unitTest --no-daemon
```
Validated: **success**, ~55s.

### 4) Integration tests
Matches CI `integration_test` job; requires Docker dependencies first:
```bash
docker compose up -d
./gradlew integrationTest --no-daemon
```
Validated: **success**, ~4m26s, 387 tests / 1 skipped.

### 5) Focused integration test
When running a single root integration test, **target the root task explicitly**:
```bash
./gradlew :integrationTest --tests 'uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.health.HealthCheckIT' --no-daemon
```
Validated: **success**, ~31s.

Do **not** use unqualified `integrationTest --tests ...` for a focused run. This repo registers `integrationTest` in the root and each subproject, and the subproject tasks fail with `No tests found for given includes`.

## Faster validation for small changes
- Do **not** run the full integration suite on every tiny edit while iterating.
- Use this scope-based rule:
  1. **Single file change**: run the smallest relevant unit test(s). Integration tests are usually **not** required.
  2. **Multiple file changes in one module**: run the relevant unit tests for that module and any focused root integration test(s) that cover the changed behavior.
  3. **Multiple file changes across multiple modules**: run the full validation flow, including the full `integrationTest` suite.
- Example focused root integration run:
```bash
docker compose up -d
./gradlew :integrationTest --tests 'uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.health.HealthCheckIT' --no-daemon
```
- Escalate to the full suite for cross-cutting changes, dependency/config changes, persistence changes, migrations, messaging/event-flow changes, or anything that may affect more than one module.

## Running the app locally
Validated startup path:
```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local --server.port=8085' --no-daemon
```
This started successfully in ~9s on port `8085`.

Notes:
- `8080` may already be in use; one validated startup failed only because port `8080` was occupied.
- README also documents fetching DEV secrets via `kubectl` for real local auth/upstream usage. If local startup or authenticated flows fail, follow `README.md` for those env vars.

## Known failure modes and workarounds
- Running integration tests without Docker deps fails fast during Flyway startup with `Connection to localhost:5434 refused`.
- `bootRun` on the default port can fail with `Port 8080 was already in use`; override `server.port`.
- `compileKotlin` installs a Git pre-push hook via the `copyGitHooks` task; this is expected.
- Build output currently contains non-failing warnings from Java 25 / Netty / deprecated APIs; these did **not** block validated commands.

## CI / pre-checkin checks to mirror locally
Workflows in `.github/workflows/` run:
- `pipeline.yml`
  - Helm lint
  - `./gradlew check -x test`
  - `./gradlew unitTest`
  - `./gradlew integrationTest`
  - Docker build, then deploys
- security scans: OWASP, Snyk, CodeQL Actions, Veracode
- `generate-sas-ui-types.yml` triggers SAS UI type generation after dev deploy

Before proposing a change, at minimum mirror CI with the full suite:
```bash
./gradlew clean check -x test --no-daemon
./gradlew unitTest --no-daemon
docker compose up -d
./gradlew integrationTest --no-daemon
```

## Useful repo-specific docs and scripts
- `README.md` — local run/test basics and architecture notes.
- `sar-test-fixtures-guide.md` — SAR fixture regeneration; note the same `:integrationTest` root-task rule.
- `docs/components/InboxEventDispatcher.md` and `docs/components/Alerting.md` — event processing/monitoring behavior.
- `docs/tools/remote-db-tunnel.md`, `docs/tools/remote-redis-tunnel.md` — remote environment access.
- `scripts/create_flyway_migration.sh` — safest way to create the next Flyway file.

## Root files worth knowing
`build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`, `docker-compose.yml`, `Dockerfile`, `detekt/detekt.yml`, `.editorconfig`, `.pre-commit-config.yaml`, `README.md`, `sonar-project.properties`, `.github/workflows/*`.

