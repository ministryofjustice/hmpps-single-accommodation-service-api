# 6. Document CAS1 Eligibility Rules and Application Journey Behaviour

Date: 2026-11-20

## Status

Proposed – Documentation of existing behaviour

## Context

Find and Manage Accommodation (FAMA) provides probation practitioners with a consolidated view of accommodation information and determines the appropriate CAS1 application journey based on a person's eligibility and existing application or placement status.

The CAS1 eligibility implementation is contained within the FAMA API and uses a rules engine to evaluate:

- The person's leaving date.
- Their current accommodation.
- Gender and CAS1 tier.
- Risk of Serious Harm (ROSH) information.
- Existing CAS1 applications.
- Existing requests for placement.
- Existing placements and their lifecycle statuses.

The implementation does not simply return whether someone is eligible for CAS1. It also determines whether the practitioner should start a new application, continue an existing draft, view a submitted application or progress an existing placement journey.

This introduces several architectural considerations.

For example, a person may already have a draft application, an active submitted application or a confirmed placement. Their circumstances may also change while an application is in progress.

The system must distinguish between eligibility to start a new application and the need to manage an existing application journey.

This ADR documents how the current FAMA implementation handles these scenarios and explains the architectural rationale for the existing behaviour.

Where the original design rationale is not explicitly documented, the rationale below is identified as an architectural interpretation of the implementation and requires confirmation from the delivery team.

## Decision

FAMA uses a rules-based approach to determine the appropriate CAS1 journey outcome based on both the person's eligibility and the lifecycle state of any existing CAS1 application or placement.

The implementation follows a defined order of evaluation:

1. Check whether the leaving date is missing or within 28 weeks.
2. Check whether an active CAS1 placement already exists.
3. Check whether an unexpired CAS1 application exists.
4. Evaluate existing placement and request-for-placement statuses.
5. Evaluate the existing application's lifecycle status.
6. Where applicable, evaluate the person's underlying CAS1 eligibility criteria.
7. Return the appropriate status and practitioner action.

The decision is to document and retain this existing behaviour, rather than introduce separate eligibility or application-state logic within FAMA.

### Scenario 1 – What happens if the practitioner has an existing draft application?

**Situation**

A practitioner has started a CAS1 application but has not yet submitted it.

**Current implementation**

FAMA checks whether an unexpired CAS1 application exists.

If the application has not been submitted, and no higher-precedence placement or active application outcome applies:

1. The application is recognised as an existing unsubmitted application.
2. FAMA sets the journey context to `Not Submitted`.
3. The normal eligibility conditions are evaluated.
4. If those conditions pass, FAMA returns `Not Submitted`.
5. The practitioner is offered `Continue application`.

If the eligibility conditions fail, FAMA returns `Not Eligible` through the normal eligibility branch.

**Why has it been designed this way?**

The architectural rationale is to preserve continuity of the application journey.

A practitioner who has already entered information should be directed towards the existing application rather than unnecessarily starting another.

This approach is intended to:

- Reduce duplicate CAS1 applications.
- Avoid unnecessary re-entry of information.
- Preserve work already undertaken by practitioners.
- Maintain a clear distinction between starting and continuing an application.

**Consequences and limitations**

The existing draft is recognised through its lifecycle status, rather than being treated as a new application.

However, the FAMA eligibility decision does not establish whether an existing draft can still be accessed directly through CAS1 when the person's eligibility subsequently fails.

That behaviour requires confirmation against the CAS1 application service.

### Scenario 2 – What happens if the person is outside the timeframe to start an application?

**Situation**

The person's leaving date is more than 28 weeks away.

**Current implementation**

FAMA evaluates the leaving date before the normal application-state branches.

If the leaving date is more than 28 weeks away:

1. The timeframe rule fails.
2. FAMA sets the journey context to `Upcoming`.
3. The normal eligibility criteria are evaluated.
4. If those criteria pass, the outcome is `Upcoming`.
5. No immediate application-start link is presented.
6. If the normal eligibility criteria fail, the outcome is `Not Eligible`.

If the leaving date is missing, the timeframe condition does not fail solely because of the missing date.

**Why has it been designed this way?**

The architectural rationale is to distinguish between a person who may be eligible for CAS1 and someone who is within the permitted application window.

A person may satisfy the underlying eligibility criteria without being at the appropriate point in their accommodation journey to start an application.

This approach is intended to:

- Prevent applications being initiated too far in advance.
- Provide visibility of potential future accommodation needs.
- Avoid treating an application timeframe restriction as a permanent eligibility failure.
- Allow practitioners to distinguish future opportunities from current actions.

**Consequences and limitations**

The `Upcoming` outcome does not provide an immediate application-start link.

The timeframe check occurs before the active application and placement branches. Therefore, an existing application does not automatically override the timeframe restriction.

The handling of an application that was previously started but whose leaving date subsequently moves outside the 28-week window requires further confirmation.

### Scenario 3 – What happens if the person has no existing CAS1 applications?

**Situation**

The person has no existing CAS1 application.

**Current implementation**

1. FAMA checks for an unexpired CAS1 application.
2. The application-present condition fails.
3. FAMA sets the journey context to `Not Started`.
4. The normal CAS1 eligibility criteria are evaluated.
5. If eligible, FAMA returns `Not Started`.
6. The practitioner is offered `Start application`.
7. If ineligible, FAMA returns `Not Eligible`.

**Why has it been designed this way?**

The architectural rationale is that the absence of an application should not automatically result in an invitation to start one.

FAMA must first determine whether the person satisfies the relevant CAS1 eligibility conditions.

This approach is intended to:

- Avoid inappropriate CAS1 applications.
- Ensure consistent application of the eligibility rules.
- Reduce unnecessary practitioner effort.
- Provide a clear entry point into the CAS1 application journey.

**Consequences and limitations**

The decision depends on the completeness and accuracy of the case information available to FAMA.

Missing or incorrect eligibility data can prevent the start action from being presented.

### Scenario 4 – What happens if the person already has a submitted CAS1 application?

**Situation**

A CAS1 application has been submitted and remains active.

**Current implementation**

1. FAMA recognises the existing unexpired application.
2. The active application branch is evaluated.
3. If the application is awaiting a placement request, the corresponding placement-request outcome is returned.
4. If further information has been requested, FAMA returns `Information Requested`.
5. Otherwise, the active application branch returns `Submitted`.
6. For `Submitted`, the practitioner is offered `View application`.

**Why has it been designed this way?**

The architectural rationale is to recognise that an application already in progress must be managed through its existing lifecycle.

Requiring the practitioner to start another application could create duplicate records and conflict with decisions already made in CAS1.

This approach is intended to:

- Preserve the existing application journey.
- Avoid duplicate submissions.
- Maintain CAS1 as the owner of application lifecycle information.
- Provide practitioners with visibility of application progress.
- Avoid treating a submitted application as though it were a new eligibility request.

**Consequences and limitations**

An active submitted application can produce a journey outcome before the normal eligibility criteria are evaluated.

This means FAMA may continue to display an existing application even where the person's current circumstances differ from those at the point of submission.

The rules engine does not itself establish that such changes invalidate the existing application.

### Scenario 5 – What happens if eligibility changes while an application is in flight?

**Situation**

A person has an existing CAS1 application, but their circumstances change before the application journey is complete.

Examples include:

- Their CAS1 tier changes.
- Their current accommodation changes.
- Their ROSH information changes.
- Their leaving date changes.

**Current implementation**

The result depends on the existing application state and the order of the rules.

For an unsubmitted application, FAMA reaches the normal eligibility branch and can return `Not Eligible` if the current criteria fail.

For an active submitted application, FAMA may return the existing application status before evaluating the normal eligibility criteria.

For an active placement, the placement branch may take precedence over the application and normal eligibility branches.

The reviewed rules do not establish that FAMA automatically withdraws, cancels or updates an existing CAS1 application when eligibility changes.

**Why has it been designed this way?**

The architectural rationale is to distinguish between determining eligibility for a new application and managing an application that has already entered the CAS1 lifecycle.

Once an application has been submitted, decisions about its progression, withdrawal or cancellation may require CAS1-specific business processes rather than an automatic change based on a FAMA eligibility result.

This approach is intended to:

- Avoid automatically disrupting existing application journeys.
- Preserve CAS1 ownership of application decisions.
- Prevent FAMA's eligibility evaluation from becoming an application lifecycle management process.
- Allow existing application and placement statuses to be represented independently of new-application eligibility.

**Consequences and limitations**

This creates an important distinction between current eligibility and existing application state.

A person may no longer satisfy the conditions for starting a new application while still having an active submitted application.

**Further confirmation required:** Whether CAS1 independently revalidates eligibility following material changes, and what process applies if an existing application must be withdrawn or amended.

### Scenario 6 – What happens if the existing application has been rejected?

**Situation**

A person's CAS1 application has status `Rejected`.

**Current implementation**

1. FAMA recognises the rejected application.
2. The journey context is set to `Application Rejected`.
3. The normal eligibility criteria are evaluated.
4. If eligible, FAMA returns `Application Rejected`.
5. The practitioner is offered `Start new application`.
6. If ineligible, FAMA returns `Not Eligible`.

**Why has it been designed this way?**

The architectural rationale is that rejection of a previous application should not necessarily prevent a subsequent application.

The person's circumstances may have changed, or a new application may be appropriate.

This approach is intended to:

- Support legitimate repeat applications.
- Avoid reusing a rejected application as though it were active.
- Ensure current eligibility is checked before another application is started.
- Preserve the distinction between previous application outcomes and current eligibility.

**Consequences and limitations**

A rejected application is not resumed through the normal draft continuation action.

The eligibility criteria must pass before a new application action is offered.

### Scenario 7 – What happens if the existing application has expired?

**Situation**

The person has a CAS1 application with status `Expired`.

**Current implementation**

1. FAMA checks for an unexpired application.
2. The check fails because the application has expired.
3. The journey context is set to `Not Started`.
4. The normal eligibility criteria are evaluated.
5. If eligible, FAMA presents `Start application`.

**Why has it been designed this way?**

The architectural rationale is to avoid treating an expired application as a valid active application.

An expired application should not automatically determine the person's current accommodation journey.

This approach is intended to:

- Avoid continuing an application that is no longer active.
- Allow current eligibility to be assessed.
- Provide a clear route for a new application.
- Preserve the historical application record separately from the current journey.

**Consequences and limitations**

The expired application does not provide a continuation action through FAMA.

The new-application route remains dependent on current eligibility.

### Scenario 8 – What happens if further information is requested?

**Situation**

A submitted CAS1 application has status `Requested Further Information`.

**Current implementation**

1. FAMA recognises the active application.
2. The further-information status is evaluated.
3. FAMA returns `Information Requested`.
4. The application link is associated with the `PROVIDE_INFORMATION` action type.

**Why has it been designed this way?**

The architectural rationale is that a request for further information forms part of the existing application journey.

It should not require a new application.

This approach is intended to:

- Allow practitioners to respond to outstanding information requests.
- Preserve the existing application.
- Reduce duplicate application activity.
- Make the next required action visible.

**Consequences and limitations**

FAMA identifies the required action but does not become the owner of the CAS1 application or its outstanding information requirements.

### Scenario 9 – What happens if a request for placement already exists?

**Situation**

The person has progressed beyond the initial CAS1 application and has an associated request for placement.

**Current implementation**

FAMA evaluates the request-for-placement status.

| Request status | FAMA outcome | Action |
|---|---|---|
| Unsubmitted | Placement Request Not Started | Create placement request |
| Submitted | Placement Request Submitted | View application |
| Rejected | Placement Request Rejected | Create new placement request |
| Withdrawn | Placement Request Withdrawn | Create new placement request |

These outcomes apply where the decision path reaches the request-for-placement branch.

**Why has it been designed this way?**

The architectural rationale is to recognise that a request for placement is a distinct stage of the CAS1 lifecycle.

A practitioner should not be directed to start another CAS1 application when the existing application has already progressed to placement-request activity.

This approach is intended to:

- Preserve continuity between application and placement stages.
- Avoid unnecessary new applications.
- Distinguish between an existing request and the need for another request.
- Reflect the current CAS1 lifecycle within FAMA.

**Consequences and limitations**

FAMA must rely on accurate request-for-placement status information.

An incorrect or outdated status could result in an inappropriate action being presented.

### Scenario 10 – What happens if the person already has a CAS1 placement?

**Situation**

A person has an existing CAS1 placement.

**Current implementation**

FAMA evaluates placement information before the normal application-state branches, where the initial timeframe condition passes.

The implementation distinguishes between several placement outcomes.

| Placement situation | FAMA outcome | Action |
|---|---|---|
| Active upcoming placement | Placement Booked | View application |
| Person has arrived | Arrived | View application |
| Placement departed | Placement Request Not Started | Create new placement request |
| Placement cancelled | Placement Cancelled | Create new placement request |
| Other non-departed, non-cancelled placement on the later placement branch | Not Arrived | Create new placement request |

**Why has it been designed this way?**

The architectural rationale is that an existing placement represents a more advanced stage of the accommodation journey than a new application.

FAMA should reflect that existing position rather than evaluate the person solely as a prospective applicant.

This approach is intended to:

- Prevent unnecessary new applications.
- Preserve visibility of existing accommodation arrangements.
- Support progression through the placement lifecycle.
- Allow further placement requests where the existing placement has ended or been cancelled.

**Consequences and limitations**

Placement state can take precedence over normal eligibility checks.

FAMA depends on the accuracy of CAS1 placement information to determine the appropriate outcome.

### Scenario 11 – What happens if mandatory eligibility information is missing?

**Situation**

The person has incomplete case information, such as a missing ROSH level or an unsupported tier.

**Current implementation**

Where the normal eligibility branch is reached:

- A missing ROSH level results in `Not Eligible`.
- A tier outside the applicable permitted values results in `Not Eligible`.
- Current accommodation outside prison or CAS1 results in `Not Eligible`.

A missing leaving date is treated differently: it does not itself cause the 28-week timeframe rule to fail.

**Why has it been designed this way?**

The architectural rationale is to avoid returning a positive eligibility outcome where required eligibility conditions cannot be satisfied.

This provides a conservative approach to eligibility evaluation.

However, the handling of a missing leaving date reflects a different rule and should not be generalised to all missing data.

**Consequences and limitations**

The `Not Eligible` outcome does not necessarily distinguish between a person who genuinely fails an eligibility criterion and one whose required information is missing.

This may limit the explanation available to practitioners and should be considered when reviewing user-facing guidance.

### Scenario 12 – What happens if there are multiple historical CAS1 applications?

**Situation**

A person has multiple CAS1 application records, potentially including expired, rejected, submitted or unsubmitted applications.

**Current implementation**

The reviewed rules engine evaluates the CAS1 application supplied to it.

The generated rules graph does not establish how the upstream integration selects the relevant application where multiple records exist.

**Why is this important?**

The architectural rationale for selecting a single relevant application would be to avoid ambiguous journey outcomes.

Without a consistent selection rule, FAMA could present an inappropriate status or action.

For example, an older rejected application should not necessarily override a newer active application.

**Consequences and limitations**

The application-selection logic must be confirmed in the FAMA integration code.

This ADR does not assert a specific ordering or prioritisation rule without supporting implementation evidence.

### Scenario 13 – What happens if CAS1 application information is unavailable?

**Situation**

FAMA cannot retrieve the required CAS1 application or placement information.

**Current implementation**

The generated eligibility decision tree does not establish the integration failure behaviour.

The relevant API client, aggregation and error-handling implementation must be reviewed.

**Why is this important?**

The architectural concern is that missing integration data must not automatically be interpreted as confirmation that no application exists.

Otherwise, FAMA could incorrectly present a new application action.

The appropriate design rationale is to distinguish between:

- Confirmed absence of an application.
- Unavailable application information.
- Incomplete or inconsistent application information.

**Consequences and limitations**

The exact existing failure response, fallback and practitioner-facing behaviour remain to be verified.

No new fallback mechanism is proposed by this ADR.

## Consequences

### Benefits

The existing approach provides several architectural benefits.

**1. Clear separation between eligibility and application lifecycle**

FAMA determines eligibility and journey status, while CAS1 remains responsible for managing applications and placements.

**2. Reduced risk of duplicate applications**

Existing draft and submitted applications are recognised before a new application action is offered on the applicable branches.

**3. Continuity of practitioner journeys**

Practitioners can continue existing applications and progress placement requests rather than repeatedly starting new applications.

**4. Consistent application of eligibility criteria**

The normal eligibility rules are centralised within the FAMA rules engine.

**5. Support for changing circumstances**

The decision tree distinguishes between current eligibility and existing application or placement lifecycle information.

**6. Explicit journey-state resolution**

The rules produce defined statuses and actions rather than leaving each frontend journey to interpret raw application data independently.

### Trade-offs

**1. Rule precedence introduces complexity**

An existing placement or active application can produce an outcome before normal eligibility is evaluated.

This means the final status cannot always be explained solely by the person's current eligibility information.

**2. Eligibility and application state may differ**

A person may no longer meet the conditions for a new application while retaining an active submitted application.

**3. Dependency on upstream information**

FAMA relies on accurate case, application and placement data to determine the appropriate outcome.

**4. Limited visibility of integration failures**

The rules graph does not describe how unavailable or inconsistent upstream data affects the practitioner journey.

**5. Missing information may be indistinguishable from ineligibility**

Some eligibility failures can result from absent data rather than a confirmed failure to meet the underlying business criterion.

### Implementation Considerations

The following areas should be verified against the existing FAMA implementation and automated tests before the ADR is accepted.

| Area | Verification required |
|---|---|
| Draft applications | Confirm continuation behaviour when eligibility subsequently fails |
| Application timeframe | Confirm behaviour where an existing application's leaving date moves outside 28 weeks |
| Submitted applications | Confirm whether CAS1 independently revalidates eligibility |
| Multiple applications | Confirm how FAMA selects the relevant application |
| Data freshness | Confirm when eligibility is recalculated and whether relevant data is cached |
| Integration failures | Confirm handling when CAS1 or case-data sources are unavailable |
| Conflicting statuses | Confirm how inconsistent application and placement information is handled |
| Action enforcement | Confirm whether CAS1 independently validates actions initiated through FAMA |
| Automated tests | Confirm coverage of each documented decision branch and edge case |

## References

### FAMA API repository

https://github.com/ministryofjustice/hmpps-single-accommodation-service-api

### CAS1 eligibility rules graph

https://ministryofjustice.github.io/hmpps-single-accommodation-service-api/eligibility-rules-graph.html#tree=CAS1

### Generated eligibility graph source

`docs/eligibility-rules-graph.md`

### Eligibility implementation

`query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/`

### Related architecture decisions

- ADR 0001 – Adopt a DDD Lite Approach for Domain Modelling.
- ADR 0003 – API Aggregation Pattern for Reads.

## Summary

The CAS1 eligibility implementation within FAMA is designed to determine the appropriate practitioner journey by combining current eligibility information with existing application and placement lifecycle state.

The principal architectural rationale is to distinguish between starting a new application and managing an existing CAS1 journey.

This supports application continuity, reduces the risk of duplication and preserves CAS1 ownership of its application and placement lifecycle.

The rules engine uses defined precedence to determine whether the practitioner should start, continue, view or progress an application.

Where the original rationale is not recorded in the implementation, this ADR provides an architectural interpretation that should be validated with the delivery team before acceptance.

**This ADR documents the existing behaviour and does not propose changes to the CAS1 eligibility rules.**
