# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for the Single Accommodation Service API. ADRs document important architectural decisions, their context, and consequences.

## What are ADRs?

Architecture Decision Records are documents that capture important architectural decisions made along with their context and consequences. They help:

- **Document decisions**: Record why decisions were made, not just what was decided
- **Share knowledge**: Help team members understand the reasoning behind architectural choices
- **Track evolution**: Show how architecture evolves over time
- **Onboard new team members**: Provide context for architectural decisions

## ADR Index

| ADR                                                              | Title                                     | Status   | Date       | Description                                                                                               |
|------------------------------------------------------------------|-------------------------------------------|----------|------------|-----------------------------------------------------------------------------------------------------------|
| [0001](./0001-adopt-a-ddd-lite-approach-for-domain-modelling.md) | Adopt DDD Lite for Domain Modeling        | Accepted | 2025-12-09 | Adopt a lightweight DDD approach suitable for an aggregation service, separating read and write models    |
| [0002](./0002-ddd-with-outbox-pattern-for-domain-events.md)      | DDD with Outbox Pattern for Domain Events | Proposed | 2025-12-10 | Implement transactional outbox pattern for reliable domain event publishing without blocking transactions |
| [0003](./0003-api-aggregation-pattern-for-reads.md)              | API Aggregation Pattern for Reads         | Proposed | 2025-12-11 | Use parallel API aggregation with caching to efficiently combine data from multiple upstream services     |
| [0004](./0004-query-nd-mutations.md)                             | Project Structure                         | Proposed | 2025-12-61 | Explaining how the project is structured using gradle                                                     |

## ADR Status

- **Proposed**: Decision is proposed and under review
- **Accepted**: Decision has been accepted and is being implemented
- **Deprecated**: Decision has been superseded by a newer ADR
- **Rejected**: Decision was considered but not adopted

## How to Create a New ADR

1. Create a new file following the naming convention: `NNNN-title-with-dashes.md`
2. Use the next sequential number (e.g., if the highest is 0008, use 0009)
3. Follow the ADR template structure:
    - Title
    - Date
    - Status
    - Context
    - Decision
    - Consequences (Benefits, Trade-offs, Implementation Considerations)
    - References

## ADR Template

```markdown
# N. Title of Decision

Date: YYYY-MM-DD

## Status

Proposed

## Context

[Describe the context and problem statement]

## Decision

[Describe the decision and approach]

## Consequences

### Benefits
- [List benefits]

### Trade-offs
- [List trade-offs]

### Implementation Considerations
- [List implementation considerations]

## References
- [Links to related ADRs and documentation]
```
