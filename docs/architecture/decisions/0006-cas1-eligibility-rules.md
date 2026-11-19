# ADR: CAS1 Eligibility Rules Evaluation and Ownership

**Status:** Proposed

## Context

The Single Accommodation Service (SAS) provides eligibility rules to support decisions about accommodation services, including CAS1.

Eligibility assessment needs to be consistent, maintainable, explainable and aligned with the relevant accommodation service's business policies.

As SAS and FAMA develop, there is a risk of duplicating eligibility rules across services or introducing inconsistent outcomes.

## Decision

We propose that:

1. SAS provides a central capability for evaluating CAS1 eligibility rules.
2. CAS1 eligibility policies remain subject to the appropriate CAS1 business ownership and governance.
3. FAMA consumes eligibility outcomes rather than independently implementing CAS1 rules.
4. Eligibility responses should provide an outcome and sufficient explanation to support practitioner decisions.
5. Rules should be versioned and tested against agreed business scenarios.
6. Eligibility evaluation should not automatically create an accommodation application or placement request.

The existing implementation and API contracts must be validated before this decision is accepted.

## Options Considered

### Option 1: Centralised evaluation through SAS (Preferred)

CAS1 eligibility rules are evaluated through SAS and made available to authorised consumers.

**Advantages:** Consistent evaluation, reduced duplication, clearer integration boundaries.

**Trade-offs:** SAS becomes a dependency and requires appropriate ownership, availability and change governance.

### Option 2: Eligibility evaluation within FAMA

FAMA implements and evaluates CAS1 eligibility rules.

**Advantages:** Direct control over the practitioner journey.

**Disadvantages:** Risks duplicating rules and creating unclear service ownership.

### Option 3: Eligibility evaluation within each CAS service

Each CAS service independently implements its own eligibility evaluation.

**Advantages:** Local service autonomy.

**Disadvantages:** Increased risk of inconsistent integration patterns and duplicated functionality.

## Consequences

- SAS eligibility evaluation requires defined API contracts and service ownership.
- Rule changes need regression testing and business approval.
- Eligibility results should distinguish the outcome from supporting explanations.
- Audit requirements must establish whether evaluated outcomes, ruleset versions and relevant inputs need retaining.
- FAMA should remain responsible for presenting the practitioner journey rather than owning CAS1 eligibility policy.

## Outstanding Questions

- Does the current CAS1 rules implementation already meet this proposed boundary?
- What is the authoritative source of each rule and its input data?
- Which eligibility outcomes are advisory, and which prevent progression?
- How are overrides and exceptions governed?
- What information needs to be persisted for audit and reporting?
- How will rule changes be approved, versioned and deployed?

## References

- [CAS1 Eligibility Rules Graph](https://ministryofjustice.github.io/hmpps-single-accommodation-service-api/eligibility-rules-graph.html#tree=CAS1)
- [SAS API Repository](https://github.com/ministryofjustice/hmpps-single-accommodation-service-api)
