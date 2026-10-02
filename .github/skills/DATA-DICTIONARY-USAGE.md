# Data Dictionary Skill — Generic Template

This skill can be used as a template for generating data dictionaries in any project. The file `DATA-DICTIONARY-SKILL-TEMPLATE.md` contains a fully generic version with customization placeholders.

## How to Use

1. **Copy the template** to your project:
   ```bash
   cp .github/skills/DATA-DICTIONARY-SKILL-TEMPLATE.md YOUR_PROJECT/.github/skills/data-dictionary/SKILL.md
   ```

2. **Replace all placeholder tokens** with your project's specifics. See the table in the template for what each placeholder means.

3. **Test the skill** with a small domain scope before running full generation.

## Customization Examples

### Example 1: Python/SQLAlchemy Project

**Find & Replace in `SKILL.md`:**

| Placeholder | Replace With |
|-------------|--------------|
| `[PROJECT_NAME]` | my-data-app |
| `[LANGUAGE/FRAMEWORK]` | Python/Flask/SQLAlchemy/Alembic |
| `[DOMAIN_LIST]` | users, orders, products, payments, audit |
| `[DOMAIN_COUNT]` | 50+ |
| `[ENTITY_PACKAGE_LIST]` | `app/models/`, `app/models/users.py`, `app/models/orders.py` |
| `[MIGRATION_PATH]` | `alembic/versions/` |
| `[MIGRATION_NAMING]` | `YYYYMMDD_HHMMSS_description.py` (Alembic convention) |
| `[REPEATABLE_PATTERN]` | Not used (Alembic uses versioned migrations only) |
| `[ENTITY_ANNOTATIONS]` | `@dataclass` or `class ... (Base):` |
| `[TABLE_NAME_SOURCE]` | `__tablename__ = "..."` or class name in snake_case |
| `[COLUMN_ANNOTATIONS]` | `Column(String, ...)`, `Column(Integer, ...)` |
| `[RELATIONSHIP_ANNOTATIONS]` | `relationship("...")`, `ForeignKey("...")` |
| `[DEPRECATION_MARKER]` | Comment `# DEPRECATED:` or `@deprecated` decorator |
| `[DOC_OUTPUT_PATH]` | `docs/schema/` |
| `[TYPE_SYSTEM]` | Python (e.g., `str`, `int`, `Optional(UUID)`) |
| `[NATIVE_QUERY_EXAMPLES]` | `.from_statement(text(...))`, `@hybrid_property`, views backed by raw SQL |

---

### Example 2: Java/EclipseLink Project

| Placeholder | Replace With |
|-------------|--------------|
| `[PROJECT_NAME]` | legacy-billing-system |
| `[LANGUAGE/FRAMEWORK]` | Java/EclipseLink/Spring Data/Liquibase |
| `[DOMAIN_LIST]` | invoices, customers, payments, tax, audit |
| `[DOMAIN_COUNT]` | 70+ |
| `[ENTITY_PACKAGE_LIST]` | `com.example.billing.entity`, `com.example.billing.entity.invoice`, `com.example.billing.entity.customer` |
| `[MIGRATION_PATH]` | `src/main/resources/db/changelog/` |
| `[MIGRATION_NAMING]` | `db.changelog-YYYYMMDD.xml` or `.sql` (Liquibase convention) |
| `[REPEATABLE_PATTERN]` | Liquibase `preconditions` for repeatable scripts |
| `[ENTITY_ANNOTATIONS]` | `@Entity @Table(name="...")` |
| `[TABLE_NAME_SOURCE]` | `@Table(name = "...")` or class name |
| `[COLUMN_ANNOTATIONS]` | `@Column(name = ...)`, `@JoinColumn(name = ...)` |
| `[RELATIONSHIP_ANNOTATIONS]` | `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany` |
| `[DEPRECATION_MARKER]` | `@Deprecated` annotation or `doc/deprecations/invoices.md` |
| `[DOC_OUTPUT_PATH]` | `docs/data-dictionary/` |
| `[TYPE_SYSTEM]` | Java (e.g., `String`, `Long`, `UUID`, `LocalDate`) |
| `[NATIVE_QUERY_EXAMPLES]` | `@Query(value = "...", nativeQuery = true)`, `@NamedQuery`, repository custom methods |

---

### Example 3: TypeScript/TypeORM Project (Node.js API)

| Placeholder | Replace With |
|-------------|--------------|
| `[PROJECT_NAME]` | typescript-api |
| `[LANGUAGE/FRAMEWORK]` | TypeScript/Express/TypeORM/TypeORM migrations |
| `[DOMAIN_LIST]` | users, posts, comments, tags, auth |
| `[DOMAIN_COUNT]` | 40+ |
| `[ENTITY_PACKAGE_LIST]` | `src/entities/`, `src/entities/User.ts`, `src/entities/Post.ts` |
| `[MIGRATION_PATH]` | `src/migrations/` |
| `[MIGRATION_NAMING]` | `YYYYMMDDHHMMSS-description.ts` (TypeORM convention) |
| `[REPEATABLE_PATTERN]` | Views defined in repeatable migration files |
| `[ENTITY_ANNOTATIONS]` | `@Entity` decorator |
| `[TABLE_NAME_SOURCE]` | `@Entity('table_name')` or class name in snake_case |
| `[COLUMN_ANNOTATIONS]` | `@Column({ name: "..." })`, `@JoinColumn()` |
| `[RELATIONSHIP_ANNOTATIONS]` | `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany` |
| `[DEPRECATION_MARKER]` | JSDoc `@deprecated` comment or separate deprecation doc |
| `[DOC_OUTPUT_PATH]` | `docs/schema/` |
| `[TYPE_SYSTEM]` | TypeScript (e.g., `string`, `number`, `UUID`, `Date`, `boolean`) |
| `[NATIVE_QUERY_EXAMPLES]` | `query('SELECT ...')`, `createQueryBuilder().rawSql()`, database views |

---

## Minimal Customization Checklist

At a minimum, replace these key placeholders:

- ✅ `[PROJECT_NAME]` — Your project name
- ✅ `[LANGUAGE/FRAMEWORK]` — Language, ORM, migration tool
- ✅ `[DOMAIN_LIST]` — Your domains (comma-separated)
- ✅ `[ENTITY_PACKAGE_LIST]` — Where entity code lives
- ✅ `[MIGRATION_PATH]` — Where migrations live
- ✅ `[MIGRATION_NAMING]` — Your migration file naming convention
- ✅ `[DOC_OUTPUT_PATH]` — Where to output the dictionary

The remaining placeholders are used in procedural steps; they're less critical if your project structure is similar to the examples above.

## Tips for Quick Adaptation

1. **Search & replace in bulk**: Open the template in your editor and use find-and-replace to swap all placeholders at once.
2. **Copy the template to a test workspace** and customize it there before committing to your project.
3. **Keep the original generic template** in a shared location so you can reuse it for other projects.
4. **Document your customizations** in your project's `SKILL.md` so future maintainers know what was changed.

## File Structure

- **`SKILL.md`** — The main skill for your project (customize the generic template and rename to this).
- **`DATA-DICTIONARY-SKILL-TEMPLATE.md`** — The generic template (keep this as reference).
- **`references/template.md`** — Output format templates (CSV and Markdown).
- **`references/README.md`** — Reference documentation.

## Support

If you're adapting this for a new ORM or framework not covered in the examples above, the key things to customize are:

1. **Entity detection**: How to find entity classes in your codebase (package scanning, file patterns, decorators, etc.).
2. **Column extraction**: How to map entity properties to database columns (annotations, naming conventions, reflection, etc.).
3. **Relationship mapping**: How to detect and represent one-to-many, many-to-one, etc.
4. **Migration parsing**: How to cross-check entity definitions against physical schema (SQL parsing, schema inference, etc.).
5. **Output formats**: CSV and Markdown templates (usually framework-agnostic, but type names and link paths may vary).

Document any non-standard steps in your project's `SKILL.md` for future reference.
