# 5. Store Payload as JSONB in PostgresSQL with String Mapping in JPA Entity

Date: 2025-02-18

## Status

Proposed

## Context

We need to store payload data (e.g. from inbox events) in PostgreSQL and represent it in our JPA entities. The payload structure may vary, and we need to balance:

- Database efficiency and queryability
- Application-side performance and memory usage
- Flexibility for future payload structure changes

We must choose both a database column type and an application-side representation that work well together.

## Decision

We will store payload data in PostgreSQL using the `jsonb` column type, while mapping the entity field as a `String` in JPA.

### Entity Mapping

```kotlin
@Column(columnDefinition = "jsonb")
@JdbcTypeCode(SqlTypes.JSON)
var payload: String
```

This allows PostgreSQL to store the data in an efficient, queryable JSONB format, while keeping the application-side representation lightweight.

### Rationale

**Database storage (JSONB)**

Using `jsonb` in PostgreSQL gives us:

- Efficient binary storage of JSON
- Ability to index and query inside JSON when required

We keep full DB-side capability without constraining payload structure.

**Application representation**

We evaluated three practical representations:

| Option | Pros | Cons |
|--------|------|------|
| **JsonNode** | Can query unknown structures; `jsonMapper.readTree` is faster when converting to a POJO | Requires full JSON parsing; creates many objects in memory; wasteful if we don't use the full payload. Too expensive for our use case. |
| **POJO mapping** | Strong typing and validation; convenient when payload structure is stable (which ours will be); good when fields are always used | Still requires full parsing; produces object graphs we may not need. Best if the structure is heavily used. |
| **String (chosen)** | Minimal memory overhead; fast entity loading; no parsing cost unless explicitly required | No compile-time typing; parsing required when payload inspection is needed |

We chose the **String representation** because parsing only occurs when necessary, not on every load, and we can decide when to parse.

## Consequences

### Benefits

- **Minimal memory overhead**: No object trees or JsonNode graphs built on load
- **Fast entity loading**: Retrieving a string from the database is cheaper than constructing JSON trees or object graphs
- **Deferred parsing cost**: JSON parsing cost is paid only when needed
- **Database flexibility**: JSONB gives us efficient storage and the ability to index and query inside JSON when required
- **Throughput under load**: Avoiding unnecessary parsing improves throughput

### Trade-offs

- **No compile-time typing**: Payload is untyped until explicitly parsed
- **Explicit parsing required**: When payload inspection is needed, we must parse manually
- **Small payloads**: For small payloads, performance differences are negligible; the main benefit is avoiding unnecessary parsing on every load

### Implementation Considerations

- Parse payload to a POJO or JsonNode only when specific fields are needed
- Consider adding helper methods or extension functions for common parsing patterns
- Document where and when parsing occurs to avoid redundant parsing

## References

- [PostgreSQL JSONB documentation](https://www.postgresql.org/docs/current/datatype-json.html)
- [Hibernate 6 JSON support](https://docs.hibernate.org/orm/6.0/userguide/html_single/#basic-mapping-json)
