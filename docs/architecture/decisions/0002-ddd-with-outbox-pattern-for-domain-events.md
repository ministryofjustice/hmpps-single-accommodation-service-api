# 2. DDD with Outbox Pattern for Domain Events

Date: 2024-12-19

## Status

Proposed

## Context

When we introduce mutations to the Single Accommodation Service API (as outlined in ADR-0001), we will need to publish domain events to notify other services of changes. These events need to be published reliably without:

- Blocking database transactions with external API calls
- Risking event loss if the transaction commits but event publishing fails
- Holding database connections during slow external operations
- Creating inconsistent state between domain changes and event publishing

Other services within HMPPS depend on data which will be mastered in the Single Accommodation Service API. When data changes occur, these services need to be notified so they can:

- Update their own systems (e.g., NDelius updates)
- Trigger downstream workflows
- Maintain data consistency across services
- Provide audit trails

Examples of information that needs to be communicated:
- A person's change of address
- The reason why an application for accommodation has been rejected
- The location where a person being released will be accommodated

The traditional approach of publishing events directly from within a transaction has several problems:

1. **Long transactions**: External event publishing can take seconds, holding database connections
2. **Connection pool exhaustion**: With many concurrent requests, connection pools can be exhausted
3. **Partial failures**: If event publishing fails after the transaction commits, we lose the event
4. **Lock contention**: Database locks are held during external operations

## Decision

We will implement **DDD with the Transactional Outbox Pattern** for reliable domain event publishing, following HMPPS-wide patterns and standards.

### Approach

1. **Domain Events in Aggregate Roots**: Following DDD Lite (ADR-0001), aggregate roots generate domain events as part of their state changes. These events are collected and written to an outbox table within the same transaction as the domain changes.

2. **Outbox Table**: A dedicated database table stores domain events that need to be published. Events are written to this table in the same transaction as domain changes, ensuring atomicity.

3. **Asynchronous Event Relay**: A separate relay process reads committed outbox rows and publishes them to AWS messaging services (SNS for domain events, SQS for point-to-point scenarios). The relay uses database polling with `SKIP LOCKED` to support concurrent workers.

4. **Event Publishing**: Events are published using AWS messaging services:
    - **SNS (Recommended)**: For HMPPS domain events following pub-sub pattern with multiple subscribers
    - **SQS (Alternative)**: For direct point-to-point messaging or when FIFO ordering is required

5. **Event Schema**: Following HMPPS guidance, we use a lightweight message format with a `detailUrl` that points to an events API endpoint providing full event details.

6. **Self-Contained Event Details**: The event details API returns self-contained information (including staff details, premises information) rather than referencing external links, making events reliable and trustworthy audit snapshots.

### Key Principles

- **Same transaction**: Events are written to the outbox in the same transaction as domain changes, ensuring atomicity
- **Short transactions**: Database transactions remain short and focused (no external API calls)
- **Retry logic**: Failed event publishing can be retried automatically with exponential backoff
- **Idempotency**: Event publishing should be idempotent to handle retries safely
- **Standards compliance**: Follows HMPPS-wide domain event patterns

## Consequences

### Benefits

- **Reliable event delivery**: Events are guaranteed to be written if the transaction commits
- **Short transactions**: Database connections are released quickly, improving throughput
- **Better resource utilization**: Connection pools aren't exhausted by long-running transactions
- **Retry capability**: Failed events can be retried without manual intervention
- **Audit trail**: Outbox table provides a complete audit trail of all events
- **Loose coupling**: Services are decoupled through events
- **Scalable**: Pub-sub pattern supports multiple subscribers
- **Standards compliance**: Follows HMPPS-wide patterns

### Trade-offs

- **Eventual consistency**: Events are published asynchronously, so there's a delay between domain change and event publishing (typically seconds)
- **Additional infrastructure**: Requires a scheduled job to process the outbox and SNS/SQS setup
- **Database overhead**: Additional table and writes for each domain change
- **Complexity**: More moving parts than direct event publishing (outbox table, relay process, monitoring)
- **Event versioning**: Need to handle event schema evolution over time
- **Detail URL availability**: Events API must be available for consumers to fetch full event details
- **Monitoring requirements**: Need to track outbox lag, failure rates, and processing metrics

### Implementation Considerations

- **Batch processing**: Process multiple events in batches for efficiency (e.g., 100 events per batch)
- **Error handling**: Failed events should be logged and potentially moved to a dead letter queue after max retry attempts
- **Monitoring**: Track outbox processing metrics (lag, failure rate, throughput, stuck messages)
- **Idempotency**: Event consumers should handle duplicate events gracefully (use dedup keys and idempotent operations)
- **Event types**: Define standard event types following the pattern:
    - `single-accommodation-service.noun.verb` (e.g., `single-accommodation-service.case.created`)
- **SNS vs SQS choice**:
    - Use **SNS** for HMPPS domain events (pub-sub, multiple subscribers, HMPPS standards)
    - Use **SQS** for direct point-to-point messaging or simpler scenarios
    - The `destination` field in outbox messages determines which pattern to use
- **Retry strategy**: Use exponential backoff for failed event publishing (1m, 5m, 15m, 1h...)
- **Concurrent relay instances**: Use `SKIP LOCKED` in SQL queries to allow multiple relay instances without conflicts

## References

- [ADR-0001: Adopt DDD Lite for Domain Modeling](./0001-adopt-a-ddd-lite-approach-for-domain-modelling.md)
- [JPA Transaction Best Practices - Outbox Pattern Implementation](../../sources/jpa-best-practices.md#outbox-pattern-reliable-event-publishing)
- [HMPPS ADR - Domain Event Schema](https://dsdmoj.atlassian.net/wiki/spaces/NDSS/pages/3732570166/ADR+-+Domain+Event+Schema)
- [HMPPS ADR - Publishing Microservice Events](https://dsdmoj.atlassian.net/wiki/spaces/NDSS/pages/2811691200/ADR+-+Publishing+Microservice+Events)