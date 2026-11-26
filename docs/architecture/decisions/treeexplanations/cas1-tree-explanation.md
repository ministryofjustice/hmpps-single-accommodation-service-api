# How the CAS1 rules graph determines a status

Date: 2026-11-25

## Purpose

This document explains how the CAS1 (Approved Premises) tree in the eligibility rules engine turns a person's data into one CAS1 status, and why the tree is ordered the way it is. The tree is built in `Cas1EligibilityTreeProvider`; the interactive graph next to this panel is generated from that code, so the graph is always the source of truth for the exact shape.

## How to read the graph

- A **ruleset** node contains one or more rules. If any rule fails, evaluation follows the **FAIL** edge; otherwise it follows the **PASS** edge.
- **Naming:** a ruleset is named after what you need to pass it (for example `NotMaleRuleSet` passes when the person is not male). A rule is named after what makes it fail (for example `MaleRule` fails when the person is male). A ruleset and the rule inside it can therefore read as opposites; this is intentional.
- An **outcome** node ends evaluation with a fixed status, such as `CAS1_ARRIVED`.
- A **context updater** sets a provisional status, such as `CAS1_NOT_STARTED`, and evaluation continues. That provisional status is returned only if every later check passes.
- `CAS1_NOT_ELIGIBLE` is returned with the failure reason of the rule that failed.

Evaluation always starts at the top and stops at the first outcome it reaches, so **earlier checks take priority over later ones**.

## Evaluation order

The tree has four stages. Each stage only runs if the previous stage did not produce a status.

### Stage 1: is a placement already active?

| Check | Data read | Result |
|---|---|---|
| Placement status is `ARRIVED` | `cas1Application.currentOutcome.placement.status` | `CAS1_ARRIVED` |
| Placement status is `UPCOMING` | as above | `CAS1_PLACEMENT_BOOKED` |
| Anything else (including no application or no placement) | | go to Stage 2 |

**Why first:** a person who is in, or booked into, an Approved Premises has a real placement regardless of anything else. The application may since have expired, or the person may no longer meet the eligibility criteria, but telling the practitioner to start a new application would be wrong. This is why Stage 1 runs before the application expiry check and before eligibility.

### Stage 2: what stage has the existing application reached?

First, the tree checks the application is usable:

| Check | Data read | Result |
|---|---|---|
| No CAS1 application, or status `EXPIRED` | `cas1Application`, `cas1Application.application.status` | set provisional `CAS1_NOT_STARTED`, go to Stage 3 |
| Otherwise | | continue below |

**Why:** with no application, or an expired one, the practitioner needs to start again, but only if the person is eligible. Stage 3 decides that.

If the application is usable, the tree looks at the most advanced piece of work first: placement, then request for placement, then the application itself.

**Inactive placement** (`currentOutcome.placement` exists):

| Placement status | Result |
|---|---|
| `DEPARTED` | `CAS1_PLACEMENT_REQUEST_NOT_STARTED` |
| `CANCELLED` | `CAS1_PLACEMENT_CANCELLED` |
| any other (for example `NOT_ARRIVED`) | `CAS1_NOT_ARRIVED` |

**Request for placement** (no placement, but `currentOutcome.requestForPlacement` exists):

| Request for placement status | Result |
|---|---|
| `REQUEST_REJECTED` | `CAS1_PLACEMENT_REQUEST_REJECTED` |
| `REQUEST_WITHDRAWN` | `CAS1_PLACEMENT_REQUEST_WITHDRAWN` |
| `REQUEST_UNSUBMITTED` | `CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED` |
| any other (for example `AWAITING_MATCH`, `PLACEMENT_BOOKED`) | `CAS1_PLACEMENT_REQUEST_SUBMITTED` |

**Application only** (no placement and no request for placement):

| Application status | Result |
|---|---|
| `PENDING_PLACEMENT_REQUEST` | `CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED` |
| `REQUESTED_FURTHER_INFORMATION` | `CAS1_INFO_REQUESTED` |
| other active statuses (`AWAITING_ASSESSMENT`, `UNALLOCATED_ASSESSMENT`, `ASSESSMENT_IN_PROGRESS`, `AWAITING_PLACEMENT`, `PLACEMENT_ALLOCATED`) | `CAS1_SUBMITTED` |
| `REJECTED` | set provisional `CAS1_APPLICATION_REJECTED`, go to Stage 3 |
| not submitted (for example `STARTED`) | set provisional `CAS1_NOT_SUBMITTED`, go to Stage 3 |
| any other submitted but closed status (`INAPPLICABLE`, `WITHDRAWN`) | set provisional `CAS1_NOT_STARTED`, go to Stage 3 |

**Why:**

- **Most advanced work first.** The practitioner's next action depends on the furthest point the case has reached. A departed placement means a new placement request is needed, even though the application is still active. That is why placement is checked before request for placement, and request for placement before the application.
- **Statuses that end here are not re-checked for eligibility.** These are submitted, in-progress applications and requests. CAS1 has already accepted or is assessing the case, and showing `CAS1_NOT_ELIGIBLE` would hide live work. The scenarios prove this: an ineligible person with an `AWAITING_PLACEMENT` application still gets `CAS1_SUBMITTED` (scenario 1122).
- **Statuses that continue to Stage 3 all mean "do something new".** That might be starting, finishing or re-applying. Asking someone to do that is only correct if they are eligible and the timing is right.

### Stage 3: is the person eligible for CAS1?

Stage 3 is reached only with a provisional status of `CAS1_NOT_STARTED`, `CAS1_NOT_SUBMITTED` or `CAS1_APPLICATION_REJECTED`. The checks run in this order, and the first failure returns `CAS1_NOT_ELIGIBLE`:

| Order | Check | Data read | Failure reason |
|---|---|---|---|
| 1 | Sex is `M`: tier must be A–C. Any other value, including not recorded: tier must be A–D | `sex`, `tierScore` | `MALE_NOT_HIGH_RISK_TIER` / `NON_MALE_NOT_HIGH_RISK_TIER` |
| 2 | Current accommodation is a prison or CAS1 | `currentAccommodationTypeEntity.isPrison`, `.isCas1` | `NOT_IN_PRISON_OR_CAS1_ACCOMMODATION` |
| 3 | A ROSH level is recorded | `roshLevel` | `HAS_NO_ROSH_LEVEL` |

**Why:**

- **Tier threshold by sex.** CAS1 is targeted at higher-tier cases. The threshold is A–C for male people and A–D for everyone else. Unknown sex uses the wider A–D rule, so a missing record never excludes someone who would otherwise qualify.
- **Current accommodation.** The tree only offers CAS1 to people currently in prison or in CAS1 accommodation; anyone else fails this check.
- **ROSH level.** The tree requires a recorded ROSH level before offering CAS1. Without one, the practitioner sees why the person is not eligible rather than being sent into an application that cannot progress.
- **Order of the checks.** Each check returns its own failure reason, and the first failure wins. Practitioners therefore see one reason at a time, tier first.

### Stage 4: is it too early to act?

| Check | Data read | Result |
|---|---|---|
| Current accommodation end date is more than 28 weeks after today | `currentAccommodation.endDate`, today | `CAS1_UPCOMING`, with an action start date of the end date minus 28 weeks |
| Within 28 weeks, or no end date | | the provisional status from Stage 2 |

**Why:**

- **Timing is checked last.** Timing only matters for someone who is eligible and needs to act. An ineligible person should see "not eligible", not "come back later".
- **It also applies to rejected and unsubmitted applications.** The practitioner is not expected to (re)apply yet, so `CAS1_UPCOMING` replaces the provisional status. It also tells them when to come back.
- **Missing end date counts as within 28 weeks.** This is a deliberate fail-open choice: the practitioner is shown the action rather than told to wait indefinitely.

## Status summary

| Status | Reached when |
|---|---|
| `CAS1_ARRIVED` | Placement `ARRIVED` (Stage 1) |
| `CAS1_PLACEMENT_BOOKED` | Placement `UPCOMING` (Stage 1) |
| `CAS1_PLACEMENT_REQUEST_NOT_STARTED` | Usable application, placement `DEPARTED` |
| `CAS1_PLACEMENT_CANCELLED` | Usable application, placement `CANCELLED` |
| `CAS1_NOT_ARRIVED` | Usable application, any other inactive placement status |
| `CAS1_PLACEMENT_REQUEST_REJECTED` | Request for placement `REQUEST_REJECTED` |
| `CAS1_PLACEMENT_REQUEST_WITHDRAWN` | Request for placement `REQUEST_WITHDRAWN` |
| `CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED` | Request for placement `REQUEST_UNSUBMITTED`, or application `PENDING_PLACEMENT_REQUEST` |
| `CAS1_PLACEMENT_REQUEST_SUBMITTED` | Any other request for placement status |
| `CAS1_INFO_REQUESTED` | Application `REQUESTED_FURTHER_INFORMATION` |
| `CAS1_SUBMITTED` | Any other active application status |
| `CAS1_APPLICATION_REJECTED` | Application `REJECTED`, eligible, within 28 weeks |
| `CAS1_NOT_SUBMITTED` | Application not yet submitted, eligible, within 28 weeks |
| `CAS1_NOT_STARTED` | No, expired, inapplicable or withdrawn application; eligible; within 28 weeks |
| `CAS1_UPCOMING` | Would be one of the previous three, but the end date is more than 28 weeks away |
| `CAS1_NOT_ELIGIBLE` | Would be one of the previous four, but a Stage 3 check failed |

## Worked examples

The scenario CSVs use a reference date of 01/01/2025, so the 28-week boundary is 16/07/2025. "Eligible" in `cas1-application-scenarios.csv` means male, tier A, in CAS1 accommodation and ROSH LOW. "Ineligible" means male, tier D.

| Scenario | Inputs | Path | Status |
|---|---|---|---|
| application #1 | Eligible, no application, no end date | Stage 1 → no application → `NOT_STARTED` → Stage 3 passes → no end date | `CAS1_NOT_STARTED` |
| application #3 | Eligible, no application, ends 17/07/2025 | As above, but the end date is 28 weeks + 1 day away | `CAS1_UPCOMING` |
| application #9 | Eligible, application `STARTED`, ends 17/07/2025 | Not submitted → `NOT_SUBMITTED` → eligible → too early | `CAS1_UPCOMING` |
| application #1784 | Eligible, application `REJECTED`, ends 16/07/2025 | Rejected → `APPLICATION_REJECTED` → eligible → exactly 28 weeks | `CAS1_APPLICATION_REJECTED` |
| application #1786 | Ineligible, application `REJECTED` | Rejected → tier D fails for a male | `CAS1_NOT_ELIGIBLE` |
| application #1122 | Ineligible, application `AWAITING_PLACEMENT` | Active application, no request or placement; eligibility not checked | `CAS1_SUBMITTED` |
| application #1251 | Eligible, request `PLACEMENT_BOOKED`, placement `DEPARTED` | Placement checked before request → departed | `CAS1_PLACEMENT_REQUEST_NOT_STARTED` |
| application #1197 | Eligible, request `AWAITING_MATCH`, no placement | Request not rejected, withdrawn or unsubmitted | `CAS1_PLACEMENT_REQUEST_SUBMITTED` |
| application #2470 | Ineligible, application `EXPIRED`, placement `ARRIVED` | Stage 1 wins before expiry or eligibility | `CAS1_ARRIVED` |
| application #2479 | Eligible, application `EXPIRED`, placement `NOT_ARRIVED` | Placement not active → application expired → start again | `CAS1_NOT_STARTED` |
| eligibility | Sex `F`, tier D, accommodation `HMP`, ROSH `HIGH` | Non-male A–D passes → prison → ROSH present | `CAS1_NOT_STARTED` |
| eligibility | Sex `M`, tier D, accommodation `HMP`, ROSH `HIGH` | Male A–C fails | `CAS1_NOT_ELIGIBLE` (`MALE_NOT_HIGH_RISK_TIER`) |
| eligibility | Sex `F`, tier A, accommodation `A02`, no ROSH | Tier passes → CAS1 accommodation → no ROSH | `CAS1_NOT_ELIGIBLE` (`HAS_NO_ROSH_LEVEL`) |

## Inputs that determine the outcome

These are the scenario columns that feed the tree. Each maps to the `DomainData` field it populates:

| Scenario column | `DomainData` field |
|---|---|
| `cas1ApplicationStatus` | `cas1Application.application.status` (blank means no application) |
| `cas1RequestForPlacementStatus` | `cas1Application.currentOutcome.requestForPlacement.status` (blank means none) |
| `cas1PlacementStatus` | `cas1Application.currentOutcome.placement.status` (blank means none) |
| `sex` | `sex` |
| `tierScore` | `tierScore` (any value outside A–C for male people or A–D otherwise, including `MISSING` and `NOT_SUPERVISED`, fails) |
| `currentAccommodationType` | `currentAccommodationTypeEntity` (`HMP` is a prison, `A02` is CAS1, `notPrisonOrCas1` is neither) |
| `RiskLevel` | `roshLevel` |
| `currentAccommodationEndDate` | `currentAccommodation.endDate` |
| `referenceDate` | today, via the injected `Clock` |
| `isEligible` | Shorthand for a passing (`TRUE`) or failing (`FALSE`) set of Stage 3 inputs |

The table below lists every value the scenarios in `cas1-eligibility-scenarios.csv` and `cas1-application-scenarios.csv` exercise. Update it when you add a scenario with a new value. _(blank)_ means the value is not recorded.

| Input field | Scenario file(s) | Values exercised by scenarios |
|---|---|---|
| `referenceDate` | `cas1-eligibility-scenarios.csv`<br>`cas1-application-scenarios.csv` | `01/01/2025` |
| `RiskLevel` | `cas1-eligibility-scenarios.csv` | _(blank)_, `HIGH`, `LOW`, `MEDIUM`, `VERY_HIGH` |
| `currentAccommodationType` | `cas1-eligibility-scenarios.csv` | _(blank)_, `A02`, `HMP`, `notPrisonOrCas1` |
| `currentAccommodationEndDate` | `cas1-eligibility-scenarios.csv`<br>`cas1-application-scenarios.csv` | _(blank)_, `16/07/2025`, `17/07/2025` |
| `sex` | `cas1-eligibility-scenarios.csv` | _(blank)_, `F`, `M`, `N`, `NS` |
| `tierScore` | `cas1-eligibility-scenarios.csv` | _(blank)_, `A`, `B`, `C`, `D`, `E`, `F`, `G`, `MISSING`, `NOT_SUPERVISED` |
| `isEligible` | `cas1-application-scenarios.csv` | `FALSE`, `TRUE` |
| `cas1ApplicationStatus` | `cas1-application-scenarios.csv` | _(blank)_, `ASSESSMENT_IN_PROGRESS`, `AWAITING_ASSESSMENT`, `AWAITING_PLACEMENT`, `EXPIRED`, `INAPPLICABLE`, `PENDING_PLACEMENT_REQUEST`, `PLACEMENT_ALLOCATED`, `REJECTED`, `REQUESTED_FURTHER_INFORMATION`, `STARTED`, `UNALLOCATED_ASSESSMENT`, `WITHDRAWN` |
| `cas1RequestForPlacementStatus` | `cas1-application-scenarios.csv` | _(blank)_, `AWAITING_MATCH`, `PLACEMENT_BOOKED`, `REQUEST_REJECTED`, `REQUEST_SUBMITTED`, `REQUEST_UNSUBMITTED`, `REQUEST_WITHDRAWN` |
| `cas1PlacementStatus` | `cas1-application-scenarios.csv` | _(blank)_, `ARRIVED`, `CANCELLED`, `DEPARTED`, `NOT_ARRIVED`, `UPCOMING` |
