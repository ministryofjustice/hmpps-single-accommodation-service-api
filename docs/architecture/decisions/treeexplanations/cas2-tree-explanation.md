# How the CAS2 rules graph determines a status

Date: 2026-11-25

## Purpose

This document explains how the CAS2 (Short-term accommodation (ISR)) tree in the eligibility rules engine turns a person's data into one CAS2 status, and why the tree is ordered the way it is. The tree is built in `Cas2EligibilityTreeProvider`; the interactive graph next to this panel is generated from that code, so the graph is always the source of truth for the exact shape.

## How to read the graph

- A **ruleset** node contains one or more rules. Every rule in the ruleset is evaluated. If any rule fails, evaluation follows the **FAIL** edge; otherwise it follows the **PASS** edge.
- **Naming:** a ruleset is named after what you need to pass it (for example `NotMaleRuleSet` passes when the person is not male). A rule is named after what makes it fail (for example `MaleRule` fails when the person is male). A ruleset and the rule inside it can therefore read as opposites; this is intentional.
- An **outcome** node ends evaluation with a fixed status, such as `CAS2_ARRIVED`.
- A **context updater** sets a provisional status, such as `CAS2_NOT_STARTED`, and evaluation continues. That provisional status is returned only if every later check passes. In this tree, context updaters sit on both PASS and FAIL edges.
- `CAS2_UPCOMING` is drawn as a terminal context updater: it replaces the provisional status, and evaluation then ends with that status.
- `CAS2_NOT_ELIGIBLE` is returned with the failure reasons of **every** failing rule in the ruleset that failed, so one ruleset can return more than one reason.

Evaluation always starts at the top and stops at the first outcome it reaches, so **earlier checks take priority over later ones**.

## Evaluation order

The tree has four stages. Each stage only runs if the previous stage did not produce a status.

### Stage 1: has a CAS2 application been submitted?

| Check | Data read | Result |
|---|---|---|
| No CAS2 application | `cas2Application` | set provisional `CAS2_NOT_STARTED`, go to Stage 3 |
| Application exists but has not been submitted | `cas2Application.application.submittedApplication.submittedAt` | set provisional `CAS2_NOT_SUBMITTED`, go to Stage 3 |
| Application submitted | | go to Stage 2 |

**Why first:** whether there is a submitted application decides which kind of status is possible. A submitted application has live or finished work at CAS2, which Stage 2 describes. With no application, or an unsubmitted one, the practitioner needs to start or finish an application, but only if the person is eligible. Stage 3 decides that.

### Stage 2: what has the assessment reached?

First, the tree checks whether the assessor has set a status at all:

| Check | Data read | Result |
|---|---|---|
| No assessment status | `cas2Application.application.submittedApplication.latestAssessmentStatus` | `CAS2_SUBMITTED` |
| Assessment status is active (`MORE_INFO_REQUESTED`, `AWAITING_DECISION`, `ON_WAITING_LIST`, `PLACE_OFFERED`, `OFFER_ACCEPTED`, `AWAITING_ARRIVAL`) | as above | continue to **active assessment** below |
| Any other status (`OFFER_DECLINED`, `CANCELLED`, `WITHDRAWN`) | as above | continue to **closed assessment** below |

**Active assessment.** The checks run in this order and the first match wins:

| Order | Check | Data read | Result |
|---|---|---|---|
| 1 | Person has been marked as arrived | `submittedApplication.markedAsArrived` | `CAS2_ARRIVED` |
| 2 | Status is `AWAITING_ARRIVAL` | `submittedApplication.latestAssessmentStatus` | `CAS2_AWAITING_ARRIVAL` |
| 3 | Status is `OFFER_ACCEPTED` | as above | `CAS2_OFFER_ACCEPTED` |
| 4 | Status is `PLACE_OFFERED` | as above | `CAS2_PLACE_OFFERED` |
| 5 | Status is `ON_WAITING_LIST` | as above | `CAS2_ON_WAITING_LIST` |
| 6 | Status is `MORE_INFO_REQUESTED` | as above | `CAS2_MORE_INFORMATION_NEEDED` |
| 7 | Anything else (in practice `AWAITING_DECISION`) | | `CAS2_AWAITING_DECISION` |

**Closed assessment:**

| Assessment status | Result |
|---|---|
| `OFFER_DECLINED` | set provisional `CAS2_OFFER_DECLINED_OR_WITHDRAWN`, go to Stage 3 |
| `CANCELLED` | set provisional `CAS2_CANCELLED`, go to Stage 3 |
| any other (in practice `WITHDRAWN`) | set provisional `CAS2_WITHDRAWN`, go to Stage 3 |

**Why:**

- **Submitted with no assessment status is its own outcome.** The application is with CAS2 but nobody has acted on it yet, so `CAS2_SUBMITTED` is returned straight away.
- **Arrival is checked before the assessment status.** Once someone is marked as arrived, that is the most advanced point the case can reach, so `CAS2_ARRIVED` wins over any active assessment status. The scenarios prove this: `AWAITING_ARRIVAL` plus a recorded arrival gives `CAS2_ARRIVED` (application scenario 12). Arrival is only looked at when the assessment status is active; it is not checked for a closed assessment or when there is no status.
- **The remaining checks follow the progression backwards.** Each active status is a single value, so after the arrival check only one of steps 2–6 can match. The order (arrival, awaiting arrival, offer accepted, place offered, waiting list, more information) reads from the most to the least advanced, with awaiting decision as the fallback.
- **Statuses that end here are not re-checked for eligibility.** These are submitted, in-progress applications. CAS2 already has the case, and showing `CAS2_NOT_ELIGIBLE` would hide live work. The scenarios prove this: an ineligible person with a `PLACE_OFFERED` assessment still gets `CAS2_PLACE_OFFERED` (application scenario 10).
- **Statuses that continue to Stage 3 all mean "start again".** A declined, cancelled or withdrawn application cannot progress, so the practitioner would need a new application. That is only correct if the person is eligible and the timing is right.

### Stage 3: is the person eligible for CAS2?

Stage 3 is reached only with a provisional status of `CAS2_NOT_STARTED`, `CAS2_NOT_SUBMITTED`, `CAS2_OFFER_DECLINED_OR_WITHDRAWN`, `CAS2_CANCELLED` or `CAS2_WITHDRAWN`. The rulesets run in this order, and the first failing ruleset returns `CAS2_NOT_ELIGIBLE`:

| Order | Check | Data read | Failure reason |
|---|---|---|---|
| 1 | Person is 18 or over (not recorded passes) | `dob`, today | `UNDER_18` |
| 1 | Tier is A–G (not recorded, `MISSING` or `NOT_SUPERVISED` fails) | `tierScore` | `INVALID_TIER` |
| 1 | No sexual offence registrations | `sexualOffenceRegistrations` | `SEXUAL_OFFENCE_REGISTRATIONS_PRESENT` |
| 2 | Not in prison without a release date | `currentAccommodationTypeEntity.isPrison`, `currentAccommodation.endDate` | `IN_PRISON_NO_RELEASE_DATE` |
| 3 | Not in CAS3 accommodation | `currentAccommodationTypeEntity.isCas3` | `IN_CAS3_ACCOMMODATION` |
| 3 | Not in CAS2 accommodation | `currentAccommodationTypeEntity.isCas2` | `IN_CAS2_ACCOMMODATION` |
| 3 | Not in the community: not prison, CAS1, CAS2 or CAS3 (no accommodation type recorded counts as community) | `currentAccommodationTypeEntity` | `IN_COMMUNITY` |
| 4 | No next accommodation | `nextAccommodations` | `HAS_NEXT_ACCOMMODATION` |
| 5 | A ROSH level is recorded | `roshLevel` | `HAS_NO_ROSH_LEVEL` |

Checks with the same order number are rules in the same ruleset. All of them are evaluated, and every one that fails adds its reason. For example, an under-18 with a missing tier and a sexual offence registration gets all three reasons.

**Why:**

- **Person first.** Age, tier and sexual offence registrations are about the person, not their situation, so they are grouped together and reported together. A missing date of birth passes, so a missing record never excludes someone on age. A missing tier fails, because only a recorded A–G tier passes.
- **Prison without a release date.** Someone in prison with no end date has no known point at which they need accommodation, so the tree reports that rather than offering CAS2. This check runs before the accommodation-type check, so a prison case without a date sees this specific reason.
- **Current accommodation.** After the prison check, only people in prison or in CAS1 accommodation can pass. CAS2, CAS3 and community accommodation each give their own reason. An unknown accommodation type is treated as community and fails.
- **Next accommodation.** If a next accommodation is already recorded, CAS2 is not offered.
- **ROSH level last.** The tree requires a recorded ROSH level before offering CAS2. Because it is checked last, a person who fails an earlier check sees that reason instead (eligibility scenario 250).
- **One ruleset at a time.** Each later ruleset only runs if the earlier ones passed, so practitioners see the reasons from the first failing ruleset only.

### Stage 4: is it too early to act?

| Check | Data read | Result |
|---|---|---|
| Current accommodation end date is more than 15 days after today | `currentAccommodation.endDate`, today | `CAS2_UPCOMING`, with an action start date of the end date minus 15 days |
| Within 15 days, in the past, or no end date | | the provisional status from Stage 1 or Stage 2 |

**Why:**

- **Timing is checked last.** Timing only matters for someone who is eligible and needs to act. An ineligible person should see "not eligible", not "come back later".
- **It applies to every provisional status.** Not started, not submitted, offer declined, cancelled and withdrawn all mean the practitioner would start or finish an application. If that is not due yet, `CAS2_UPCOMING` replaces the provisional status and tells them when to come back.
- **Missing end date counts as within 15 days.** This is a fail-open choice: the practitioner is shown the action rather than told to wait. A person in prison without an end date never reaches this stage, because Stage 3 has already returned `IN_PRISON_NO_RELEASE_DATE`.

## Status summary

| Status | Reached when |
|---|---|
| `CAS2_ARRIVED` | Submitted, active assessment status, marked as arrived |
| `CAS2_AWAITING_ARRIVAL` | Submitted, assessment `AWAITING_ARRIVAL`, not marked as arrived |
| `CAS2_OFFER_ACCEPTED` | Submitted, assessment `OFFER_ACCEPTED`, not marked as arrived |
| `CAS2_PLACE_OFFERED` | Submitted, assessment `PLACE_OFFERED`, not marked as arrived |
| `CAS2_ON_WAITING_LIST` | Submitted, assessment `ON_WAITING_LIST`, not marked as arrived |
| `CAS2_MORE_INFORMATION_NEEDED` | Submitted, assessment `MORE_INFO_REQUESTED`, not marked as arrived |
| `CAS2_AWAITING_DECISION` | Submitted, assessment `AWAITING_DECISION`, not marked as arrived |
| `CAS2_SUBMITTED` | Submitted, no assessment status yet |
| `CAS2_OFFER_DECLINED_OR_WITHDRAWN` | Assessment `OFFER_DECLINED`, eligible, within 15 days |
| `CAS2_CANCELLED` | Assessment `CANCELLED`, eligible, within 15 days |
| `CAS2_WITHDRAWN` | Assessment `WITHDRAWN`, eligible, within 15 days |
| `CAS2_NOT_SUBMITTED` | Application not yet submitted, eligible, within 15 days |
| `CAS2_NOT_STARTED` | No application, eligible, within 15 days |
| `CAS2_UPCOMING` | Would be one of the previous five, but the end date is more than 15 days away |
| `CAS2_NOT_ELIGIBLE` | Would be one of the previous six, but a Stage 3 check failed |

## Worked examples

The scenario CSVs use a reference date of 01/01/2025, so the 15-day boundary is 16/01/2025. An end date of 17/01/2025 is one day outside it and gives an action start date of 02/01/2025. Every row in `cas2-eligibility-scenarios.csv` has no CAS2 application, and `testCaseId` values repeat once per ROSH level, so those examples also give the ROSH level.

"Eligible" in `cas2-application-scenarios.csv` means aged exactly 18, tier A, no sexual offence registrations, ROSH LOW, no next accommodation and in CAS1 accommodation (`A02`). "Ineligible" means the same, but the accommodation is also flagged as CAS2, so Stage 3 fails with `IN_CAS2_ACCOMMODATION`.

| Scenario | Inputs | Path | Status |
|---|---|---|---|
| application #13 | Eligible, not submitted, no end date | Not submitted → `NOT_SUBMITTED` → Stage 3 passes → no end date | `CAS2_NOT_SUBMITTED` |
| application #1 | Ineligible, not submitted | Not submitted → CAS2 accommodation fails | `CAS2_NOT_ELIGIBLE` |
| application #53 | Eligible, not submitted, ends 17/01/2025 | Not submitted → eligible → 15 days + 1 day away | `CAS2_UPCOMING` |
| application #2 | Ineligible, submitted, no assessment status | Submitted, nobody has acted yet; eligibility not checked | `CAS2_SUBMITTED` |
| application #54 | Eligible, submitted, no assessment status, ends 17/01/2025 | As above; timing not checked | `CAS2_SUBMITTED` |
| application #10 | Ineligible, submitted, `placeOffered` | Active status → not arrived → place offered; eligibility not checked | `CAS2_PLACE_OFFERED` |
| application #12 | Ineligible, submitted, `awaitingArrival`, marked as arrived | Active status → arrival checked first | `CAS2_ARRIVED` |
| application #17 | Eligible, submitted, `cancelled` | Closed → `CANCELLED` → eligible → no end date | `CAS2_CANCELLED` |
| application #5 | Ineligible, submitted, `cancelled` | Closed → `CANCELLED` → CAS2 accommodation fails | `CAS2_NOT_ELIGIBLE` |
| application #20 | Eligible, submitted, `offerDeclined` | Closed → `OFFER_DECLINED_OR_WITHDRAWN` → eligible | `CAS2_OFFER_DECLINED_OR_WITHDRAWN` |
| application #47 | Eligible, submitted, `withdrawn`, ends 16/01/2025 | Closed → `WITHDRAWN` → eligible → exactly 15 days | `CAS2_WITHDRAWN` |
| application #55 | Eligible, submitted, `cancelled`, ends 17/01/2025 | Closed → `CANCELLED` → eligible → too early | `CAS2_UPCOMING` |
| eligibility #57 (ROSH `LOW`) | `HMP`, ends 16/01/2025, tier A–G, date of birth unknown, no registrations, no next accommodation | No application → `NOT_STARTED` → Stage 3 passes → exactly 15 days | `CAS2_NOT_STARTED` |
| eligibility #61 (ROSH `LOW`) | As #57, but ends 17/01/2025 | As above, but too early | `CAS2_UPCOMING` |
| eligibility #53 (ROSH `LOW`) | `HMP`, no end date, tier A–G | Person passes → in prison without a release date | `CAS2_NOT_ELIGIBLE` (`IN_PRISON_NO_RELEASE_DATE`) |
| eligibility #133 (ROSH `LOW`) | Under 18, tier `MISSING`/`NOT_SUPERVISED`, sexual offence registrations, no accommodation recorded | Person ruleset fails on all three rules; accommodation never checked | `CAS2_NOT_ELIGIBLE` (`UNDER_18`, `INVALID_TIER`, `SEXUAL_OFFENCE_REGISTRATIONS_PRESENT`) |
| eligibility #85 (ROSH `LOW`) | `A02`, ends 16/01/2025, tier not recorded | Missing tier fails | `CAS2_NOT_ELIGIBLE` (`INVALID_TIER`) |
| eligibility #5 (ROSH `LOW`) | `A01A`, no end date, tier A–G | Not prison, CAS1, CAS2 or CAS3 | `CAS2_NOT_ELIGIBLE` (`IN_COMMUNITY`) |
| eligibility #29 (ROSH `LOW`) | `A10` or `A11`, no end date, tier A–G | In CAS2 accommodation | `CAS2_NOT_ELIGIBLE` (`IN_CAS2_ACCOMMODATION`) |
| eligibility #250 (no ROSH) | `HMP`, ends 16/01/2025, next accommodation recorded | Next accommodation is checked before ROSH | `CAS2_NOT_ELIGIBLE` (`HAS_NEXT_ACCOMMODATION`) |
| eligibility #17 (no ROSH) | `A02`, no end date, tier A–G, no next accommodation | Every earlier check passes → no ROSH level | `CAS2_NOT_ELIGIBLE` (`HAS_NO_ROSH_LEVEL`) |

## Inputs that determine the outcome

These are the scenario columns that feed the tree. Each maps to the `DomainData` field it populates:

| Scenario column | `DomainData` field |
|---|---|
| `cas2ApplicationStatus` | `cas2Application.application.submittedApplication.latestAssessmentStatus` (blank means no assessment status; `any` expands to every status) |
| `submittedAt` | `cas2Application.application.submittedApplication.submittedAt` (blank means the application has not been submitted) |
| `markedAsArrived` | `cas2Application.application.submittedApplication.markedAsArrived` (`TRUE` records an arrival) |
| `isEligible` | Shorthand for a passing (`TRUE`) or failing (`FALSE`) set of Stage 3 inputs; `FALSE` flags the current accommodation as CAS2 |
| `u18` | `dob` (`TRUE` is one day short of 18, `FALSE` is exactly 18, blank means not recorded) |
| `tierScore` | `tierScore` (`A-G` expands to each tier A to G; `MISSING-OR-NOT-SUPERVISED` expands to `MISSING` and `NOT_SUPERVISED`) |
| `hasSexualOffenceRegistrations` | `sexualOffenceRegistrations` |
| `currentAccommodationType` | `currentAccommodation.type` and `currentAccommodationTypeEntity` (`HMP` is a prison, `A02` is CAS1, `A10`/`A11` are CAS2, `A17` is CAS3, anything else such as `A01A` is community; blank means no current accommodation) |
| `currentAccommodationEndDate` | `currentAccommodation.endDate` |
| `hasNextAccommodation` | `nextAccommodations` (`TRUE` adds one next accommodation) |
| `RiskLevel` | `roshLevel` |
| `referenceDate` | today, via the injected `Clock` |

The table below lists every value the scenarios in `cas2-eligibility-scenarios.csv` and `cas2-application-scenarios.csv` exercise. Update it when you add a scenario with a new value. _(blank)_ means the value is not recorded.

| Input field | Scenario file(s) | Values exercised by scenarios |
|---|---|---|
| `referenceDate` | `cas2-eligibility-scenarios.csv`<br>`cas2-application-scenarios.csv` | `01/01/2025` |
| `RiskLevel` | `cas2-eligibility-scenarios.csv` | _(blank)_, `HIGH`, `LOW`, `MEDIUM`, `VERY_HIGH` |
| `hasNextAccommodation` | `cas2-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
| `u18` | `cas2-eligibility-scenarios.csv` | _(blank)_, `FALSE`, `TRUE` |
| `currentAccommodationType` | `cas2-eligibility-scenarios.csv` | _(blank)_, `A01A`, `A02`, `A10-A11`, `A17`, `HMP` |
| `currentAccommodationEndDate` | `cas2-eligibility-scenarios.csv`<br>`cas2-application-scenarios.csv` | _(blank)_, `16/01/2025`, `17/01/2025` |
| `tierScore` | `cas2-eligibility-scenarios.csv` | _(blank)_, `A-G`, `MISSING-OR-NOT-SUPERVISED` |
| `hasSexualOffenceRegistrations` | `cas2-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
| `isEligible` | `cas2-application-scenarios.csv` | `FALSE`, `TRUE` |
| `submittedAt` | `cas2-application-scenarios.csv` | _(blank)_, `01/01/2025` |
| `markedAsArrived` | `cas2-application-scenarios.csv` | `FALSE`, `TRUE` |
| `cas2ApplicationStatus` | `cas2-application-scenarios.csv` | _(blank)_, `awaitingArrival`, `awaitingDecision`, `cancelled`, `moreInfoRequested`, `offerAccepted`, `offerDeclined`, `onWaitingList`, `placeOffered`, `withdrawn` |
