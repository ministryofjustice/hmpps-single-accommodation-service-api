# 1. Adopt DDD Lite for Domain Modeling

Date: 2025-12-09

## Status

Accepted

## Context

The Single Accommodation Service API is fundamentally an aggregation and orchestration service. It aggregates data from multiple upstream APIs (Probation Integration Delius, OASys, Tier, Core Person Record, Approved Premises) rather than being the primary owner of most of the data it displays.

The service is currently focussed on read-only. Mutations and write operations are planned for the 1st quarter of 2026 delivery. As an aggregation service, the typical DDD building blocks don't map cleanly to our domain:

- We cannot rely on projections from our own database
- We cannot use hydrators that source from our own tables
- We cannot create aggregate roots that fully encapsulate invariants for read-only operations
- Heavy DDD patterns would introduce unnecessary ceremony and overhead for simple data aggregation

However, when we introduce mutations, we will need structured patterns to manage domain logic, ensure consistency, and handle side effects (events, API updates, database writes).

## Decision

We will adopt a **DDD Lite** approach that provides the benefits of Domain-Driven Design without the full ceremony:

### Phase 1 (Read-Only)

For read operations, we will use:
- **API Aggregator** as the projection layer (replacing DB projections)
- **DTOs** for data transformation and presentation
- **Simple service layer** for orchestration and transformation
- No aggregate roots or domain entities for reads

The aggregator library acts as our "projection layer", transforming upstream API responses into DTOs for the frontend. These DTOs are not part of any aggregate root, keeping read and write models separate (as DDD recommends).

### Phase 2 (Mutations)

When we introduce write operations, we will:
- **Introduce aggregate roots** only for the mutation side
- **Hydrate aggregates** using data from upstream APIs (via the aggregator)
- **Apply state changes** to the aggregate
- **Derive side effects** from the aggregate:
    - What data needs saving to our database
    - What events need to be published (via outbox pattern)
    - Which APIs need to be notified (if any)

This keeps mutation logic centralised, consistent, and testable, without forcing heavy DDD patterns into the read path.

## Consequences

### Benefits

- **Appropriate complexity**: Avoids over-engineering for read-only operations
- **Clear separation**: Read models (DTOs) remain separate from write models (aggregates)
- **Structured mutations**: When writes are introduced, we have a clear pattern to follow
- **Testability**: Aggregate roots provide a clear boundary for testing domain logic
- **Flexibility**: Can evolve towards full DDD if needed, but not forced to adopt it prematurely

### Trade-offs

- **Two models**: We maintain separate read and write models, which requires coordination
- **Hydration overhead**: Aggregates need to be hydrated from upstream APIs, which adds complexity
- **Learning curve**: Team needs to understand when to use DDD Lite vs simple DTOs

### Alignment with Service Characteristics

This approach aligns well with SAS being fundamentally an **orchestration + aggregation service**, not a domain authority:

- Reads are simple, stateless transformations (no DDD needed)
- Writes need structured patterns (aggregate roots) to avoid spaghetti policies
- The service doesn't own most data, so full DDD doesn't fit naturally

## References

- [Best Practices](../../sources/spring-api-best-practices.md)
