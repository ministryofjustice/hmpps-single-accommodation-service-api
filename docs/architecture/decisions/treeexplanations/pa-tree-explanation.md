# How the PA rules graph determines a status

Date: 2026-11-25

## Purpose

This document explains how the PA (Proposed accommodation) tree in the eligibility rules engine turns a person's data into one PA status, and why the tree is ordered the way it is. The tree is built in `PaEligibilityTreeProvider`; the interactive graph next to this panel is generated from that code, so the graph is always the source of truth for the exact shape.

## How to read the graph

- A **ruleset** node contains one or more rules. Every rule in the ruleset is evaluated. If any rule fails, evaluation follows the **FAIL** edge; otherwise it follows the **PASS** edge.
- **Naming:** a ruleset is named after what you need to pass it (for example `NotMaleRuleSet` passes when the person is not male). A rule is named after what makes it fail (for example `MaleRule` fails when the person is male). A ruleset and the rule inside it can therefore read as opposites; this is intentional.
- An **outcome** node ends evaluation with a fixed status, such as `PA_COMPLETED`.
- `PA_NOT_ELIGIBLE` is returned with the failure reasons of every rule that failed in the ruleset immediately before it.

The PA tree has no context updaters: it starts with no provisional status, and every path ends at a fixed outcome. Evaluation always starts at the top and stops at the first outcome it reaches, so **earlier checks take priority over later ones**.

## Evaluation order

The tree has two stages. Stage 2 only runs if Stage 1 did not produce a status.

### Stage 1: does the person already have a next accommodation?

| Check | Data read | Result |
|---|---|---|
| At least one next accommodation is recorded | `nextAccommodations` | `PA_COMPLETED` |
| The list is empty | | go to Stage 2 |

**Why first:** if a next accommodation is already known, there is nothing left to propose, so the tree reports the work as complete without looking at anything else. This means an active CAS1 or CAS3 application never changes the result once a next accommodation exists (scenarios 1, 3 and 5).

`nextAccommodations` is calculated before the tree runs, in `AccommodationSummaryCalculator.calculateNextAccommodations`. It combines an `UPCOMING` CAS1 placement, a `CONFIRMED` CAS3 booking, and Core Person Record addresses with a proposed status (`PR` or `PR1`), a postcode and no end date. The rule itself only checks whether the list is empty (`NoNextAccommodationRule`); this ruleset fails without a failure reason.

### Stage 2: is a CAS1 or CAS3 application still active?

Both rules are evaluated, so both failure reasons can be returned together:

| Check | Data read | Failure reason |
|---|---|---|
| CAS1 application status is one of `AWAITING_ASSESSMENT`, `UNALLOCATED_ASSESSMENT`, `ASSESSMENT_IN_PROGRESS`, `AWAITING_PLACEMENT`, `PLACEMENT_ALLOCATED`, `REQUESTED_FURTHER_INFORMATION`, `PENDING_PLACEMENT_REQUEST` | `cas1Application.application.status` | `SUITABLE_CAS1_APPLICATION` |
| CAS3 application status is `SUBMITTED` or `REQUESTED_FURTHER_INFORMATION` | `cas3Application.applicationStatus` | `SUITABLE_CAS3_APPLICATION` |

| Outcome of the checks | Result |
|---|---|
| Either check fails | `PA_NOT_ELIGIBLE`, with every failure reason from this stage |
| Both pass | `PA_NOT_STARTED` |

A missing application passes its check, as does any status not listed: for CAS1 that is `STARTED`, `REJECTED`, `INAPPLICABLE`, `WITHDRAWN` and `EXPIRED`; for CAS3 it is `IN_PROGRESS` and `REJECTED`. Only the application status is read; the CAS1 request for placement, CAS1 placement and CAS3 booking are not.

**Why:**

- **An active application already covers the next accommodation.** If CAS1 or CAS3 is still assessing or placing the person, the tree does not ask the practitioner to add a proposed address as well; it returns `PA_NOT_ELIGIBLE` and says which application is responsible.
- **All reasons are reported.** Both rules sit in one ruleset, so a person with both applications active gets both reasons (scenario 2) rather than only the first.
- **Otherwise, start proposing.** With no next accommodation and no active application, `PA_NOT_STARTED` is returned. Its proposed action is `ADD_AND_CONFIRM_PROPOSED_ADDRESS`.

## Status summary

| Status | Reached when |
|---|---|
| `PA_COMPLETED` | At least one next accommodation exists (Stage 1) |
| `PA_NOT_ELIGIBLE` | No next accommodation, and a CAS1 and/or CAS3 application is active (Stage 2) |
| `PA_NOT_STARTED` | No next accommodation, and neither application is active (Stage 2) |

`PA_UPSTREAM_ERROR` is never returned by the tree. It is set outside the tree by `EligibilityTransformer.toFailedEligibilityDto` when the eligibility data cannot be built.

## Worked examples

`pa-eligibility-scenarios.csv` has no reference date and the tree reads no dates, so there are no date boundaries. In the scenario test, `isSubmittedCas1 = TRUE` builds a CAS1 application with status `PLACEMENT_ALLOCATED`, and `isSubmittedCas3 = TRUE` builds a CAS3 application with status `SUBMITTED`.

| Scenario | Inputs | Path | Status |
|---|---|---|---|
| PA #1 | Next accommodation, CAS1 and CAS3 active | Stage 1 passes; Stage 2 is not reached | `PA_COMPLETED` |
| PA #7 | Next accommodation, no applications | Stage 1 passes | `PA_COMPLETED` |
| PA #2 | No next accommodation, CAS1 and CAS3 active | Stage 1 fails → both Stage 2 rules fail | `PA_NOT_ELIGIBLE` (`SUITABLE_CAS1_APPLICATION`, `SUITABLE_CAS3_APPLICATION`) |
| PA #4 | No next accommodation, CAS3 active | Stage 1 fails → CAS3 rule fails | `PA_NOT_ELIGIBLE` (`SUITABLE_CAS3_APPLICATION`) |
| PA #6 | No next accommodation, CAS1 active | Stage 1 fails → CAS1 rule fails | `PA_NOT_ELIGIBLE` (`SUITABLE_CAS1_APPLICATION`) |
| PA #8 | No next accommodation, no applications | Stage 1 fails → Stage 2 passes | `PA_NOT_STARTED` |

## Inputs that determine the outcome

These are the scenario columns that feed the tree. Each maps to the `DomainData` field it populates:

| Scenario column | `DomainData` field |
|---|---|
| `hasNextAccommodation` | `nextAccommodations` (`TRUE` is one accommodation summary, `FALSE` is an empty list) |
| `isSubmittedCas1` | `cas1Application` (`TRUE` is an application with status `PLACEMENT_ALLOCATED`, `FALSE` is no application) |
| `isSubmittedCas3` | `cas3Application` (`TRUE` is an application with status `SUBMITTED`, `FALSE` is no application) |

The table below lists every value the scenarios in `pa-eligibility-scenarios.csv` exercise. Update it when you add a scenario with a new value. _(blank)_ means the value is not recorded.

| Input field | Scenario file(s) | Values exercised by scenarios |
|---|---|---|
| `hasNextAccommodation` | `pa-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
| `isSubmittedCas1` | `pa-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
| `isSubmittedCas3` | `pa-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
