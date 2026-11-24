# Document CAS1 Eligibility Rules and Application Journey Behaviour in FAMA

Date: 2026-11-20

## Status

 Proposed

## Context

Find and Manage Accommodation (FAMA) provides probation practitioners with a consolidated view of accommodation eligibility and supports them in identifying and progressing appropriate accommodation journeys.

For CAS1 (Approved Premises), FAMA uses a rules-based implementation to determine a person's eligibility and the appropriate application or placement journey.

The decision is not based solely on whether someone meets the underlying CAS1 eligibility criteria. It also considers existing applications, requests for placement, placement statuses and the timeframe within which an application can be started.

This creates several scenarios that must be handled consistently, including:

- A practitioner has an existing draft application.
- A person is outside the permitted timeframe to start an application.
- A person has no existing applications.
- A person already has a submitted application.
- A person's eligibility changes while an application is in progress.
- An application is rejected, expired or requires further information.
- A request for placement or an actual placement already exists.
- Eligibility information is missing or incomplete.

The purpose of this AD R is to document **how the current CAS1 journey operates within FAMA, why the design is appropriate and what consequences arise from the decisions**.

It also establishes a reference for future changes to the eligibility rules and application scenarios.

The documented behaviour is based on the FAMA eligibility decision graph and associated test scenarios. Where the original design rationale is not explicitly recorded, the explanations below represent architectural rationale to be validated during review.

## Decision

FAMA will continue to use its existing rules-based approach to determine the appropriate CAS1 eligibility and application journey outcome.

The implementation combines current eligibility information with the lifecycle state of existing CAS1 applications and placements.

The rules are evaluated in a defined order:

1. Check whether the leaving date is missing or within 28 weeks.
2. Check for an existing active CAS1 placement.
3. Check for an existing unexpired CAS1 application.
4. Evaluate existing placement and request-for-placement statuses.
5. Evaluate the application's lifecycle status.
6. Evaluate the normal CAS1 eligibility criteria where required.
7. Return the appropriate journey status and practitioner action.

This ordering means that an existing application or placement may determine the journey outcome before the normal eligibility criteria are evaluated.

### 1. Existing draft applications

**Scenario:** A practitioner has started a CAS1 application but has not submitted it.

**Current behaviour**

Where an existing unexpired application is identified as unsubmitted, and no higher-precedence application or placement outcome applies, FAMA sets the journey context to `Not Submitted`.

The normal eligibility criteria are then evaluated.

If those criteria pass, FAMA returns `CAS1_NOT_SUBMITTED` and presents the **Continue application** action.

If the eligibility criteria fail, FAMA returns `CAS1_NOT_ELIGIBLE`.

**Why this design?**

The rationale is to preserve continuity of an existing application rather than requiring the practitioner to start again.

This approach:

- Reduces the risk of duplicate applications.
- Preserves information already entered.
- Avoids unnecessary practitioner effort.
- Distinguishes an application in progress from a new application.

**Consequences**

An existing draft is recognised and can be continued when the applicable eligibility criteria pass.

However, the rules graph does not establish whether the underlying CAS1 application remains directly accessible if eligibility subsequently fails.

This requires confirmation against the CAS1 application service.

### 2. Outside the permitted application timeframe

**Scenario:** The person's leaving date is more than 28 weeks away.

**Current behaviour**

FAMA evaluates the leaving date at the beginning of the CAS1 decision process.

If the leaving date is more than 28 weeks away:

- The journey context is set to `Upcoming`.
- The normal CAS1 eligibility criteria are evaluated.
- If the remaining criteria pass, FAMA returns `CAS1_UPCOMING`.
- No immediate application-start link is presented.
- If eligibility fails, FAMA returns `CAS1_NOT_ELIGIBLE`.

A missing leaving date does not, by itself, fail the timeframe check.

**Why this design?**

The rationale is to distinguish between someone who may qualify for CAS1 and someone who is within the permitted timeframe to start an application.

This approach:

- Prevents applications being started prematurely.
- Provides visibility of potential future accommodation needs.
- Separates timeframe restrictions from underlying eligibility.
- Supports practitioners in understanding when an application journey may become available.

**Consequences**

The person may be considered potentially eligible but unable to start an application immediately.

Because the timeframe check precedes the active application and placement branches, the impact of a changed leaving date on an existing application requires particular attention.

### 3. No existing CAS1 applications

**Scenario:** A person has no existing CAS1 application.

**Current behaviour**

FAMA identifies that no unexpired CAS1 application exists and sets the journey context to `Not Started`.

The normal eligibility criteria are then evaluated.

If eligible, FAMA returns `CAS1_NOT_STARTED` and presents **Start application**.

If ineligible, FAMA returns `CAS1_NOT_ELIGIBLE`.

**Why this design?**

The rationale is that having no existing application should not automatically make a person eligible to start one.

Eligibility must be established before the start action is presented.

This approach:

- Ensures consistent application of CAS1 eligibility rules.
- Avoids inappropriate applications.
- Reduces unnecessary work.
- Provides a clear entry point into the CAS1 journey.

**Consequences**

The ability to start an application depends on the availability and accuracy of the underlying eligibility information.

### 4. Existing submitted application

**Scenario:** A person already has an active submitted CAS1 application.

**Current behaviour**

FAMA recognises the existing unexpired application and evaluates its lifecycle status.

Where no more specific placement or application outcome applies, FAMA returns `CAS1_SUBMITTED` and presents **View application**.

If the application is awaiting further information or a placement request, the corresponding more specific outcome is returned instead.

**Why this design?**

The rationale is to preserve an established application journey rather than encouraging a practitioner to create another application.

This approach:

- Reduces duplicate applications.
- Preserves continuity of the existing application.
- Reflects the current CAS1 lifecycle.
- Directs practitioners towards the appropriate next action.

**Consequences**

An active submitted application can produce a journey outcome before the normal eligibility checks are evaluated.

The presence of an existing application does not necessarily mean that the person would still satisfy every criterion for starting a new application.

### 5. Eligibility changes while an application is in progress

**Scenario:** A person's eligibility information changes after an application has been started or submitted.

Examples include changes to:

- CAS1 tier.
- Current accommodation.
- ROSH information.
- Leaving date.

**Current behaviour**

FAMA evaluates the supplied case and application information according to the existing decision tree.

For an unsubmitted application, the normal eligibility checks are evaluated and can return `CAS1_NOT_ELIGIBLE`.

For an active submitted application, the application lifecycle may determine the outcome before the normal eligibility checks are reached.

For an active placement, the placement status may take precedence over the application lifecycle.

The reviewed eligibility rules do not establish that FAMA automatically cancels, withdraws or updates an existing CAS1 application when eligibility changes.

**Why this design?**

The architectural rationale is to separate the decision about whether someone can start an application from the management of an application that already exists.

Once an application has been submitted, changes to its lifecycle may require CAS1-specific decisions and business processes.

This approach:

- Preserves the existing application journey.
- Avoids automatic cancellation solely through FAMA eligibility evaluation.
- Maintains a clear boundary between eligibility evaluation and application lifecycle management.
- Allows existing application and placement statuses to be represented independently of new-application eligibility.

**Consequences**

A person may have an active submitted application even where their current circumstances would not satisfy the criteria for starting a new application.

The process for reassessing, withdrawing or amending an existing application following a material eligibility change requires confirmation with the CAS1 team.

### 6. Rejected or expired applications

**Scenario:** A previous CAS1 application has been rejected or has expired.

**Current behaviour**

For a rejected application, FAMA sets the journey context to `Application Rejected` and evaluates current eligibility.

If eligible, the outcome is `CAS1_APPLICATION_REJECTED`, with the action **Start new application**.

For an expired application, FAMA treats the application as not active for the relevant application-present check.

If current eligibility passes, the outcome is `CAS1_NOT_STARTED`, with the action **Start application**.

**Why this design?**

The rationale is that a previous rejected or expired application should not automatically prevent a future application.

At the same time, these applications should not be treated as active journeys that can simply be continued.

This approach:

- Supports new applications where circumstances permit.
- Preserves the distinction between historical and active applications.
- Ensures current eligibility is assessed.
- Avoids continuing applications that are no longer active.

**Consequences**

A new application action is dependent on current eligibility.

Historical application records remain separate from the newly initiated journey.

### 7. Further information requested

**Scenario:** A submitted CAS1 application requires additional information.

**Current behaviour**

FAMA recognises the active application with status `Requested Further Information`.

It returns `CAS1_INFO_REQUESTED` and associates the application link with the `PROVIDE_INFORMATION` action.

**Why this design?**

The rationale is that providing additional information is part of the existing application journey.

Starting a new application would unnecessarily duplicate work and could disrupt the current application process.

**Consequences**

The practitioner is directed to progress the existing application rather than create another.

CAS1 remains responsible for the application and its outstanding information requirements.

### 8. Existing requests for placement

**Scenario:** A CAS1 application has progressed to the request-for-placement stage.

**Current behaviour**

FAMA evaluates the existing request status and returns the appropriate outcome.

| Request status | FAMA outcome | Practitioner action |
|---|---|---|
| Unsubmitted | `CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED` | Create placement request |
| Submitted | `CAS1_PLACEMENT_REQUEST_SUBMITTED` | View application |
| Rejected | `CAS1_PLACEMENT_REQUEST_REJECTED` | Create new placement request |
| Withdrawn | `CAS1_PLACEMENT_REQUEST_WITHDRAWN` | Create new placement request |

**Why this design?**

The rationale is to distinguish between starting a CAS1 application and progressing an existing application towards a placement.

This approach:

- Preserves the application journey.
- Prevents unnecessary new applications.
- Supports the next appropriate placement activity.
- Reflects the request-for-placement lifecycle.

**Consequences**

FAMA relies on the accuracy of the request-for-placement information supplied by CAS1.

An incorrect status could lead to an inappropriate practitioner action.

### 9. Existing CAS1 placements

**Scenario:** A person has an existing CAS1 placement.

**Current behaviour**

FAMA considers placement status as part of its journey evaluation.

Depending on the applicable branch, the outcomes include:

| Placement situation | FAMA outcome | Practitioner action |
|---|---|---|
| Active upcoming placement | `CAS1_PLACEMENT_BOOKED` | View application |
| Arrived placement | `CAS1_ARRIVED` | View application |
| Departed placement | `CAS1_PLACEMENT_REQUEST_NOT_STARTED` | Create new placement request |
| Cancelled placement | `CAS1_PLACEMENT_CANCELLED` | Create new placement request |
| Other applicable non-arrived placement state | `CAS1_NOT_ARRIVED` | Create new placement request |

These outcomes are subject to the earlier rules, including the leaving-date check.

**Why this design?**

The rationale is that an existing placement represents a more advanced stage of the accommodation journey than a new application.

FAMA should reflect the current placement position rather than treating the person as a new applicant.

This approach:

- Maintains visibility of existing placements.
- Avoids unnecessary applications.
- Supports progression through the accommodation lifecycle.
- Allows subsequent placement activity where appropriate.

**Consequences**

Placement information can take precedence over normal eligibility checks.

FAMA depends on accurate placement information to determine the correct journey outcome.

### 10. Missing eligibility information

**Scenario:** Some of the information required to evaluate CAS1 eligibility is missing or does not satisfy the applicable rules.

**Current behaviour**

Where the normal eligibility branch is reached:

- A missing ROSH level results in `CAS1_NOT_ELIGIBLE`.
- An unsupported tier results in `CAS1_NOT_ELIGIBLE`.
- Current accommodation outside prison or CAS1 results in `CAS1_NOT_ELIGIBLE`.
- A missing leaving date does not automatically fail the 28-week check.

The tier rules currently represented in the decision graph are:

| Recorded gender | Permitted tiers |
|---|---|
| Male | A, B or C |
| Not Male | A, B, C or D |

**Why this design?**

The rationale is to apply the defined eligibility conditions consistently and avoid presenting a positive eligibility outcome where required conditions are not satisfied.

The missing leaving-date behaviour is a specific exception within the existing rules.

**Consequences**

Some `Not Eligible` outcomes may arise from missing information rather than a confirmed substantive eligibility failure.

This distinction may need to be explained to practitioners.

### 11. Multiple historical applications

**Scenario:** A person has more than one historical CAS1 application.

**Current behaviour**

The rules engine evaluates the application information supplied to it.

The generated decision graph does not establish how the relevant application is selected where multiple historical applications exist.

**Why is this important?**

The architectural objective should be to ensure that FAMA evaluates the correct current application rather than allowing an older record to determine the journey incorrectly.

**Consequences**

The application-selection logic requires verification against the FAMA integration implementation.

This ADR does not assert an unverified selection or prioritisation rule.

### 12. Upstream data unavailable or inconsistent

**Scenario:** CAS1 application or placement information is unavailable, incomplete or inconsistent.

**Current behaviour**

The generated eligibility graph does not define the integration error-handling or fallback behaviour.

This must be verified against the FAMA API aggregation and integration implementation.

**Why is this important?**

The architectural objective is to distinguish between:

- A confirmed absence of an application.
- An application whose information cannot be retrieved.
- Conflicting or incomplete application information.

These are not equivalent situations.

**Consequences**

Treating unavailable data as confirmation that no application exists could lead to an incorrect start-application action.

The actual existing failure-handling behaviour must be confirmed before this ADR is accepted.

## Consequences

### Benefits

- **Consistent eligibility evaluation:** CAS1 eligibility criteria are evaluated through the existing FAMA rules engine.
- **Application continuity:** Existing applications are recognised, allowing practitioners to continue or progress the appropriate journey.
- **Reduced duplication:** Practitioners are not unnecessarily directed to create new applications when relevant active applications or placements already exist.
- **Clear ownership:** FAMA evaluates eligibility and journey status, while CAS1 retains responsibility for application and placement lifecycle management.
- **Traceability:** The documented scenarios provide a reference for understanding and testing existing behaviour.
- **Maintainability:** Future changes can be assessed against the documented architectural rationale and expected outcomes.

### Trade-offs

- **Rule precedence:** Existing application and placement states can take precedence over normal eligibility checks.
- **Changing eligibility:** An active submitted application may remain visible even when the person's current eligibility information has changed.
- **Data dependency:** FAMA relies on upstream case, application and placement information.
- **Complexity:** The final journey outcome depends on several interconnected rules rather than a single eligibility condition.
- **Incomplete failure coverage:** Integration failures and some application-selection behaviours are not fully described by the eligibility graph.

### Implementation Considerations

The ADR must remain aligned with the implemented rules and automated scenario tests.

The following behaviours require confirmation during review:

1. How FAMA selects the relevant CAS1 application where multiple applications exist.
2. Whether existing drafts remain accessible when eligibility changes.
3. How CAS1 handles material eligibility changes after submission.
4. How changes to the leaving date affect existing applications.
5. How FAMA handles unavailable or inconsistent upstream information.
6. Whether the underlying CAS1 service independently validates application actions.
7. Whether automated tests cover all documented decision branches and edge cases.

### ADR maintenance requirement

The CAS1 ADR must be reviewed whenever changes are made to the relevant eligibility rules, application scenarios or expected outcomes.

In particular, changes affecting CAS1 behaviour within:

`query/src/test/resources/`

must trigger a review of this ADR.

The relevant test files are:

- `cas1-application-scenarios.csv`
- `cas1-eligibility-scenarios.csv`

Where a change alters the documented behaviour or architectural rationale, the ADR must be updated as part of the same change.

The generated eligibility rules graph should also be reviewed whenever the underlying rules change.

This ensures that the ADR, implementation, generated decision graph and automated test scenarios remain consistent.

## References

### 1. FAMA eligibility and application test scenarios

https://github.com/ministryofjustice/hmpps-single-accommodation-service-api/tree/main/query/src/test/resources

Contains the scenario-based test resources used to validate eligibility and application journey outcomes.

Relevant changes to CAS1 scenarios or expected outcomes must trigger an ADR review.

### 2. FAMA eligibility rules graph

https://ministryofjustice.github.io/hmpps-single-accommodation-service-api/eligibility-rules-graph.html

Provides the generated visual representation of eligibility rules, evaluation order and journey outcomes for CAS1, CAS2 and other supported accommodation services.

The CAS1 decision tree is the primary reference for the rule ordering documented in this ADR.

## Decision Summary

The existing CAS1 implementation within FAMA combines eligibility criteria with application and placement lifecycle information to determine the appropriate practitioner journey.

The architectural rationale is to preserve existing application journeys, reduce unnecessary duplication and distinguish between eligibility to start a new application and the management of an existing application or placement.

FAMA owns eligibility and journey-status evaluation, while CAS1 remains responsible for the underlying application and placement lifecycle.

This ADR documents the current implementation and its architectural justification.

It does not propose changes to the existing CAS1 eligibility rules.

The documented rationale and outstanding integration behaviours must be confirmed through development review before the ADR is accepted.
