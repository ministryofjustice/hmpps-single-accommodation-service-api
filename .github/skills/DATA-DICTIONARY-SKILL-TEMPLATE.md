---
name: data-dictionary
description: 'Generate or update a data dictionary documenting the database schema for [PROJECT_NAME]. USE FOR: building a reference of entities, database tables, columns, data types, relationships, and enums; documenting the schema for [DOMAIN_LIST]; producing table-to-entity mappings; onboarding docs for the data model. Trigger words: data dictionary, schema documentation, entity reference, table catalog, document the database, ERD, column reference.'
argument-hint: '[domain: DOMAIN1 | DOMAIN2 | ... | all] [format: csv | markdown | both] [diagram: on | off]'
---
code
# Data Dictionary Generator — Generic Template

Produce a structured, accurate data dictionary for the database schema of this [LANGUAGE/FRAMEWORK] repository by reading the ORM entities (source of truth for the object model) and database migrations (source of truth for the physical schema).

> **This is a template.** Customize the sections marked as placeholders for your project. See [Customization](#customization) and [Auto-Discovery](#auto-discovery) below.

## Auto-Discovery

This skill can auto-discover most project configuration without prompting. On first use, the skill will:

1. **Inspect build files** (`build.gradle.kts`, `pom.xml`, `package.json`, etc.) to detect the language, framework, and ORM (e.g., Kotlin/Spring Boot/JPA).
2. **Search entity code** for files using `[ENTITY_ANNOTATIONS]` to locate entity packages and infer domain structure.
3. **Locate migration folders** (e.g., `src/main/resources/db/migration/`) and detect the migration naming convention (Flyway, Liquibase, custom).
4. **Scan entity annotations** to infer how table names, column names, relationships, and enums are declared.
5. **Find deprecation markers** by searching for `@Deprecated` or a `deprecations/` documentation folder.

Once the skill has discovered these details, it **only prompts the user for**:
- Which domain(s) to document (if ambiguous or for scope confirmation).
- Output format, diagram preference, and write mode (if not specified in the request).
- Any other configuration truly specific to the project's practices that cannot be inferred from code.

**Result:** You can use this skill immediately after customizing the placeholders below with your project's name and general structure. The first run will auto-discover the rest.

## Customization

**Minimal setup required.** The skill will auto-discover most configuration. You only need to manually fill in the **placeholders marked "❌ No"** in the table below:

- `[PROJECT_NAME]` — Your project's human-readable name. Used in headings and descriptions.
- `[DOC_OUTPUT_PATH]` — Where the skill should write generated documents. Usually `doc/data-dictionary/` or `docs/schema/`.

All other placeholders (marked "✅ Yes") will be discovered automatically on the skill's first run by inspecting build files, entity code, migrations, and deprecation markers. After filling in just these two fields, the skill is ready to use.

### Optional: Manual Overrides

If you want to override what auto-discovery will find (e.g., if it misidentifies domains or ORM patterns), you can manually fill in any of the other placeholders. However, **you do not need to do this** on first use. Start with the minimal setup above.

### Placeholder Reference Table

| Placeholder | Replace With | Auto-Discoverable? | Example |
|-------------|--------------|-------------------|---------|
| `[PROJECT_NAME]` | Your project name | ❌ No | hmpps-approved-premises-api, my-data-app, acme-crm |
| `[LANGUAGE/FRAMEWORK]` | Language, ORM, and framework stack | ✅ Yes (from build files) | Kotlin/Spring Boot/JPA/Hibernate, Python/SQLAlchemy, Java/EclipseLink, TypeScript/TypeORM, C#/.NET EF Core |
| `[DOMAIN_LIST]` | Your application's domain/service areas | ✅ Mostly (from package structure) | domain-a, domain-b, domain-c OR users, orders, products, payments OR api, core, admin |
| `[DOMAIN_COUNT]` | Typical entity count for a full run | ✅ Yes (by counting entity files) | 80+, 50+, 200+ (informs whether to confirm before generating) |
| `[ENTITY_PACKAGE_LIST]` | List of entity package locations | ✅ Yes (by searching for `@Entity` files) | `src/main/kotlin/.../jpa/entity/`, `.../entity/domaina/`, etc. |
| `[MIGRATION_PATH]` | Path to database migrations | ✅ Yes (by locating migration folders) | `src/main/resources/db/migration/`, `migrations/`, `db/migrations/` |
| `[MIGRATION_NAMING]` | Migration file naming convention | ✅ Yes (by inspecting migration files) | `YYYYMMDDhhmmss__description.sql` (Flyway), `YYYYMMDD_HHmmss_description.sql` (Liquibase), `001_initial.sql` (simple) |
| `[REPEATABLE_PATTERN]` | Pattern for repeatable/non-versioned migrations | ✅ Yes (by scanning migration folder) | `R__` prefix (Flyway), `V` prefix with no date (custom), `repeatable/` folder (custom) |
| `[ENTITY_ANNOTATIONS]` | Your ORM's entity markup | ✅ Yes (by inspecting actual entity code) | `@Entity @Table(name="...")` (JPA), `@dataclass`, decorator (Python), `Entity`, `attribute` (Ruby) |
| `[TABLE_NAME_SOURCE]` | How table name is determined | ✅ Yes (by analyzing entity annotations) | `@Table(name = "...")` or class name (JPA), database column convention, migration file |
| `[COLUMN_ANNOTATIONS]` | ORM column markup | ✅ Yes (by scanning entities) | `@Column(name = ...)`, `db.Column()`, `column(:name)` |
| `[RELATIONSHIP_ANNOTATIONS]` | ORM relationship markup | ✅ Yes (by scanning entities) | `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany` (JPA) or equivalent |
| `[DEPRECATION_MARKER]` | How to mark deprecated entities | ✅ Yes (by searching for `@Deprecated`, deprecation docs) | `@Deprecated` annotation, decorator, comment, separate deprecation doc |
| `[DOC_OUTPUT_PATH]` | Where to write generated docs | ❌ No (project-specific convention) | `doc/data-dictionary/`, `docs/schema/`, `documentation/api/` |
| `[TYPE_SYSTEM]` | Language's type system | ✅ Yes (inferred from language) | `Kotlin` (e.g. `String`, `UUID?`), `Python` (e.g. `str`, `Optional(UUID)`), `Java` (e.g. `String`, `UUID`) |
| `[NATIVE_QUERY_EXAMPLES]` | Your ORM's native query patterns | ✅ Yes (by inspecting code for `@Query`, `.raw()`, etc.) | `@Query(value = "...", nativeQuery = true)` (JPA), `.raw()` (SQLAlchemy), repository methods |

---

## When to Use

- The user asks to create, regenerate, or update a data dictionary / schema reference.
- Documenting tables, columns, types, relationships, or enums for one or more domains.
- Onboarding material describing the data model.

## Where Things Live

| Concern | Location |
|---------|----------|
| Entity code | `[ENTITY_PACKAGE_LIST]` |
| Database migrations | `[MIGRATION_PATH]` (naming: `[MIGRATION_NAMING]`; repeatable: `[REPEATABLE_PATTERN]`) |
| Deprecation docs | `doc/deprecations/`, `docs/deprecated/`, or inline comments |
| Domain conventions | Document URL or local path (e.g., `doc/how-to/entity-best-practices.md`) |

## Domain Scope

Your project's domains are: **[DOMAIN1, DOMAIN2, DOMAIN3, ...]** (customize based on your architecture).

If the user names a domain, limit the work to that scope. If unspecified, ask which domain(s) to document — generating all `[DOMAIN_COUNT]` entities at once is large, so default to confirming scope before a full run.

## Generation Options

This skill is configurable. Read any options the user gives in their request; for anything not specified, use the **default** and (for large runs) confirm before generating. Don't interrogate the user for every option — infer sensible defaults and state what you chose.

| Option | Values | Default | Effect |
|--------|--------|---------|--------|
| `domain` | DOMAIN1 \| DOMAIN2 \| ... \| `all` | ask / confirm | Which domain(s) to document. `all` is a large run — confirm first. |
| `format` | `csv` \| `markdown` \| `both` | `both` | Which artefact(s) to produce. `csv` = machine-readable only; `markdown` = wiki/Confluence document only; `both` = CSV + markdown kept in sync. |
| `diagram` | `on` \| `off` | `on` | Whether the markdown document includes the entity–relationship diagram link. Ignored when `format = csv`. |
| `tables` | `on` \| `off` | `on` | Whether the markdown document includes the full per-table column tables. Ignored when `format = csv`. |
| `write-mode` | `overwrite` \| `update` \| `dry-run` | `update` | `overwrite` regenerates files wholesale; `update` refreshes in place (preserving hand-written prose/notes where possible); `dry-run` reports what would change without writing. |
| `index` | `on` \| `off` | `on` | Whether to create/refresh `[DOC_OUTPUT_PATH]README.md`. |

Notes on combinations:

- `format = csv` skips the markdown document entirely (`diagram`/`tables` have no effect).
- `format = markdown` produces only `<domain>.md`. If a `<domain>.csv` already exists it is used as the data source; otherwise extract from entities directly.
- `diagram = off` with `tables = off` and `format = markdown` produces only the heading + sources — warn the user this is near-empty and confirm.
- Always honour the chosen `domain` scope regardless of other options.

## Output

Produce, per domain in scope, under `[DOC_OUTPUT_PATH]`, the artefact(s) selected by the `format` option (default `both`):

1. **`<domain>.csv`** (when `format` is `csv` or `both`) — the machine-readable dictionary, one row per column. Standard columns:
   `domain,table,entity,column,sql_type,[LANGUAGE]_type,nullable,key,enum_values,relationship,notes`
   See the [CSV template](./references/template.md) for exact formatting and quoting rules.

2. **`<domain>.md`** (when `format` is `markdown` or `both`) — a **wiki-ready** (Confluence-friendly) document containing, subject to the `diagram` and `tables` options:
   - a per-domain **entity–relationship diagram link** (when `diagram = on`); see below, and
   - the **full dictionary as markdown tables** (one table per database table, one row per column) when `tables = on`,
     so the document can be pasted/imported into a wiki such as Confluence without the CSV.
   See the [markdown template](./references/template.md).

3. **`erd-<domain>.mermaid`** (when `format` is `markdown` or `both` and `diagram = on`) — a standalone diagram file. Format depends on your preference: Mermaid ER diagram, PlantUML, or other supported format. The markdown file links to this file.

When `format = both`, the CSV and the markdown tables must contain the **same data** — the CSV is the source of truth for machine use, the markdown is the human/wiki view. Keep them in sync.

When `index = on` (default), also maintain `[DOC_OUTPUT_PATH]README.md` as an index linking each domain's generated artefact(s).

## Procedure

1. **Auto-discover project configuration.** Before asking the user for anything, search the repository to infer:
   - **Technology stack** from `build.gradle.kts`, `pom.xml`, `package.json`, or equivalent (e.g., "Kotlin + Spring Boot + JPA" from gradle).
   - **Entity packages** by searching for files marked with `[ENTITY_ANNOTATIONS]` (e.g., `@Entity` in Kotlin/Java).
   - **Migration paths** by locating `db/migration/`, `flyway/`, `liquibase/`, or equivalent folders.
   - **Domain structure** by analyzing package names, folder organization, or entity naming patterns.
   - **ORM conventions** (e.g., `@Table(name = ...)` usage, `@Column` patterns) by inspecting actual entity code.
   - **Deprecation markers** by searching for `@Deprecated`, deprecation comments, or a known deprecation document location.
   
   Once discovered, populate the placeholders in memory and state what was found. Only prompt the user for fields that **cannot** be auto-discovered.

2. **Prompt for missing information.** If any of the following are still unknown, ask the user:
   - **Domain list** (if not apparent from package structure): *"I found entity packages for `payments`, `users`, and `reports`. Are these your domain boundaries, or should I group differently?"*
   - **Write mode preference** (if not specified in the request): *"Should I overwrite existing files, update them in place, or perform a dry-run?"*
   - **Scope confirmation** (if `all` domains and `[DOMAIN_COUNT]` entities are in scope): *"I found [N] entities across [M] domains. Generating all may be large. Which domain(s) should I document?"*
   - **Other options** not specified in the request, using defaults stated clearly.

3. **Confirm scope and options.** Summarize the resolved options (tech stack discovered, entity count, domain scope, format, diagram, write mode). For an `all` run or when `write-mode = overwrite` would replace an existing dictionary, confirm first. If `write-mode = dry-run`, perform extraction and report the diff/counts but do not write files.

4. **Enumerate entities in scope.** List the entity files in the relevant package(s) (`[ENTITY_PACKAGE_LIST]`). For each entity (`[ENTITY_ANNOTATIONS]`), extract:
   - **Table name** from `[TABLE_NAME_SOURCE]`.
   - **Entity class name** and source file path.
   - **Domain** (DOMAIN1 / DOMAIN2 / ...) based on package or naming convention.
   - **Whether it is a physical table.** An entity is a *physical table* only if it has a backing `CREATE TABLE` migration. Treat as a **query-backed projection** (not a physical table) any entity that has no backing `CREATE TABLE`/`CREATE VIEW` and is hydrated by a [NATIVE_QUERY_EXAMPLES] — e.g., UNION queries, computed columns, or dynamic views. Treat as a **view-backed** entity any whose backing object is a `CREATE VIEW`. Record which kind each entity is; these do not go in the physical `## Tables` section.
   - **Deprecation status.** Check for `[DEPRECATION_MARKER]` markers; also cross-reference against known deprecation docs. Record the deprecation message if present.

5. **Extract columns.** For each entity property, capture:
   - Column name from `[COLUMN_ANNOTATIONS]`, else the property name.
   - [LANGUAGE] type and nullability.
   - Key/constraint markers: PK, FK, UNIQUE, defaults.
   - Enum columns — record the enum type and its allowed values.
   - Audit fields (created_at, updated_at, version, etc.).

6. **Extract relationships.** Record each relationship (one-to-many, many-to-one, one-to-one, many-to-many) from `[RELATIONSHIP_ANNOTATIONS]` with its target entity, join column / join table, and cardinality. Note inheritance hierarchies.

7. **Cross-check against migrations and extract queries.** Search `[MIGRATION_PATH]` for the table's `CREATE TABLE` / `ALTER TABLE` / `CREATE VIEW` statements. Reconcile entity and migration; flag mismatches. For projections and views, extract and record the **native SQL query** (e.g., from code or migration). If an entity has **no** `CREATE TABLE`/`CREATE VIEW`, classify it as a query-backed projection (see step 4).

8. **Write the CSV** (when `format` is `csv` or `both`). Emit one row per column following the [CSV template](./references/template.md). Sort by `table`, then by column order in the entity. Quote any field containing commas.

9. **Write the markdown document** `<domain>.md` (when `format` is `markdown` or `both`). Generate, per the resolved options:
   - a diagram link to a separate `.mermaid` (or equivalent) file (when `diagram = on`; see step 10 below);
   - the **full dictionary as markdown tables** — one `### <table>` section per database table (when `tables = on`); **each `### <table>` heading must include both the entity class name *and* the physical table name**, plus any deprecation status;
   - a separate `## Query-backed projections (not physical tables)` section for any projection/view-backed entities, with the **SQL query** for each;
   - a `## Sources` table with workspace-relative links to entity and migration sources.
   See the [markdown template](./references/template.md).

   **Link paths:** the files live in `[DOC_OUTPUT_PATH]`, so links to repo sources are relative to that folder. Use the conventions of your project (e.g., `../../src/`, `../`, `./`).

10. **Create the separate diagram file** `erd-<domain>.[EXT]` (when `format` is `markdown` or `both` and `diagram = on`). Generate an entity–relationship diagram showing each physical table and its relationships. Query-backed projections and view-backed entities are excluded from the diagram. The markdown document links to this file.

11. **Update the index** `[DOC_OUTPUT_PATH]README.md` (when `index = on`) to link the new/updated artefact(s) for each domain.

12. **Validate.** Confirm every in-scope entity is represented in the produced artefact(s). For the CSV: table names match migrations, relationships are bidirectionally consistent, enum value lists are complete. For the markdown: entity counts, column counts, link validity. Report counts and any unresolved flags.

## Quality Checks

Apply the checks relevant to the produced artefact(s):

- Table name in the CSV matches the entity `[TABLE_NAME_SOURCE]` **and** the migration `CREATE TABLE` — unless the entity is a query-backed projection or view-backed entity, which by definition has no `CREATE TABLE`.
- Deprecated entities are flagged clearly so consumers know not to use them.
- Nullability and types reflect the source code; `sql_type` reflects the migration.
- Every relationship names a concrete target entity and join mechanism.
- Enum columns list all values in `enum_values`, not just the type name.
- When the markdown is produced: column counts match the CSV.
- All source links in `<domain>.md` resolve.
- No entity in scope is silently skipped.

## Notes

- Entities/classes are the authoritative object model; migrations/DDL are authoritative for physical column types, defaults, and indexes. When they disagree, document both and flag it.
- Not every entity is a physical table. Query-backed projections and view-backed entities describe derived result shapes, not stored columns. Document them under `## Query-backed projections (not physical tables)` with their backing SQL/code and label each clearly so readers don't treat them as real tables.
- Include the **physical table name** in the markdown entity line (alongside the entity class name) so teams can map between code and database artifacts.
- Flag **deprecated entities** in the markdown header so consumers know not to use them.
- Keep the dictionary regenerable: re-running this skill should reproduce the same structure so diffs reflect real schema changes.
