# How the CRS rules graph determines a status

Date: 2026-11-26

## Purpose

This document explains how the CRS (Commissioned Rehabilitative Services) tree in the eligibility rules engine turns a person's data into one CRS status, and why the tree is ordered the way it is. The tree is built in `CrsEligibilityTreeProvider`; the interactive graph next to this panel is generated from that code, so the graph is always the source of truth for the exact shape.

## How to read the graph

- A **ruleset** node contains one rule. If the rule fails, evaluation follows the **FAIL** edge; otherwise it follows the **PASS** edge.
- **Naming:** a ruleset is named after what you need to pass it (for example `NotMaleRuleSet` passes when the person is not male). A rule is named after what makes it fail (for example `MaleRule` fails when the person is male). A ruleset and the rule inside it can therefore read as opposites; this is intentional.
- An **outcome** node ends evaluation with a fixed status, such as `CRS_SUBMITTED`.
- A **context updater** sets a provisional status, such as `CRS_NOT_STARTED_REFERRAL`, and evaluation continues. That provisional status is returned only if every later check passes.
- `CRS_NOT_REQUIRED` is returned with the failure reason of the rule that failed. This tree never returns a "not eligible" status: a person who does not need a referral is shown as not required.

Evaluation always starts at the top and stops at the first outcome it reaches, so **earlier checks take priority over later ones**.

## Evaluation order

The tree has three stages. Each stage only runs if the previous stage did not produce a status.

### Stage 1: is there already a live referral?

| Check | Data read | Result |
|---|---|---|
| CRS referral status is `LIVE` | `commissionedRehabilitativeServices.status` | `CRS_SUBMITTED`, with the CRS UI URL |
| Anything else (`COMPLETED`, `WITHDRAWN`, or no referral) | | set provisional status (below), go to Stage 2 |

The provisional status depends on sex:

| Sex | Provisional status |
|---|---|
| `M` | `CRS_NOT_STARTED_ACCOMMODATION_REFERRAL` |
| Any other value, including not recorded | `CRS_NOT_STARTED_REFERRAL` |

Both provisional statuses carry the CRS UI URL.

**Why first:** a live referral is existing work, so the practitioner should see it regardless of anything else. The scenarios prove this: a settled person with a live referral still gets `CRS_SUBMITTED` (scenario 30), as does someone with a next accommodation (scenario 12) or an end date more than 12 weeks away (scenario 17). A `COMPLETED` or `WITHDRAWN` referral is treated the same as no referral, so the practitioner is pointed towards a new one.

### Stage 2: does the person need a referral?

| Order | Check | Data read | Result on failure |
|---|---|---|---|
| 1 | Current accommodation is not settled | `currentAccommodationTypeEntity.settledType` | `CRS_NOT_REQUIRED` (`IS_SETTLED`) |
| 2 | The person has no next accommodation | `nextAccommodations` | `CRS_NOT_REQUIRED` (`HAS_NEXT_ACCOMMODATION`) |

If both checks pass, evaluation goes to Stage 3.

**Why:**

- **Not required, rather than not eligible.** These checks describe whether a new referral is needed, not whether the person qualifies. A settled person, or someone with somewhere to go next, has no accommodation need for a referral to address.
- **Only `SETTLED` fails the first check.** A `TRANSIENT` settled type passes, and so does a missing accommodation type (no `currentAccommodationTypeEntity`), so missing data never hides the action.
- **Any next accommodation fails the second check.** The rule only looks at whether `nextAccommodations` is empty; it does not look at the type or dates of the next accommodation.
- **Order of the checks.** The first failure wins, so a settled person who also has a next accommodation sees only `IS_SETTLED` (scenario 28).
- **After Stage 1.** These checks run only when there is no live referral, so they never hide existing work.

### Stage 3: is it too early to act?

| Check | Data read | Result |
|---|---|---|
| Current accommodation end date is more than 12 weeks after today | `currentAccommodation.endDate`, today | `CRS_UPCOMING_ACCOMMODATION_REFERRAL` if sex is `M`, otherwise `CRS_UPCOMING_REFERRAL`; no URL; an action start date of the end date minus 12 weeks |
| Within 12 weeks (end date on or before today plus 12 weeks), or no end date | | the provisional status from Stage 1 |

**Why:**

- **Timing is checked last.** Timing only matters for someone who needs a referral. A person who does not need one should see "not required", not "come back later".
- **The upcoming status keeps the male / non-male split.** It mirrors the provisional status, so the practitioner is told which kind of referral to make and when to come back. No URL is returned because there is nothing to act on yet.
- **Missing end date counts as within 12 weeks.** The practitioner is shown the action rather than told to wait indefinitely. Exactly 12 weeks away also counts as within the window, because the comparison is `endDate <= today + 12 weeks`.

## Status summary

| Status | Reached when |
|---|---|
| `CRS_SUBMITTED` | CRS referral status is `LIVE` (Stage 1) |
| `CRS_NOT_REQUIRED` | No live referral, and the person is settled (`IS_SETTLED`) or has a next accommodation (`HAS_NEXT_ACCOMMODATION`) |
| `CRS_NOT_STARTED_ACCOMMODATION_REFERRAL` | No live referral, sex `M`, not settled, no next accommodation, within 12 weeks or no end date |
| `CRS_NOT_STARTED_REFERRAL` | As above, but sex is not `M` or not recorded |
| `CRS_UPCOMING_ACCOMMODATION_REFERRAL` | Would be `CRS_NOT_STARTED_ACCOMMODATION_REFERRAL`, but the end date is more than 12 weeks away |
| `CRS_UPCOMING_REFERRAL` | Would be `CRS_NOT_STARTED_REFERRAL`, but the end date is more than 12 weeks away |

## Worked examples

The scenario CSV uses a reference date of 01/01/2025, so the 12-week boundary is 26/03/2025. Scenarios use an end date of either 01/01/2025 (within 12 weeks), 01/07/2025 (outside 12 weeks) or none. Blank sex means not recorded, and a blank settled type means no accommodation type is recorded.

| Scenario | Inputs | Path | Status |
|---|---|---|---|
| crs #1 | Referral `LIVE`, ends 01/01/2025 | Live referral | `CRS_SUBMITTED` |
| crs #17 | Referral `LIVE`, ends 01/07/2025 | Live referral; timing not checked | `CRS_SUBMITTED` |
| crs #30 | Referral `LIVE`, `SETTLED`, ends 01/07/2025 | Live referral; settled not checked | `CRS_SUBMITTED` |
| crs #12 | Referral `LIVE`, has next accommodation | Live referral; next accommodation not checked | `CRS_SUBMITTED` |
| crs #4 | Referral `COMPLETED`, ends 01/01/2025 | Not live → not settled → no next accommodation → within 12 weeks | `CRS_NOT_STARTED_REFERRAL` |
| crs #5 | Referral `WITHDRAWN`, ends 01/01/2025 | As above | `CRS_NOT_STARTED_REFERRAL` |
| crs #11 | No referral, no end date | Not live → not settled → no next accommodation → no end date | `CRS_NOT_STARTED_REFERRAL` |
| crs #20 | Sex `M`, no referral, ends 01/01/2025 | Not live → not settled → no next accommodation → within 12 weeks | `CRS_NOT_STARTED_ACCOMMODATION_REFERRAL` |
| crs #26 | No referral, `SETTLED` | Not live → settled | `CRS_NOT_REQUIRED` (`IS_SETTLED`) |
| crs #28 | No referral, `SETTLED`, has next accommodation | Not live → settled; next accommodation not checked | `CRS_NOT_REQUIRED` (`IS_SETTLED`) |
| crs #13 | No referral, has next accommodation | Not live → not settled → has next accommodation | `CRS_NOT_REQUIRED` (`HAS_NEXT_ACCOMMODATION`) |
| crs #16 | No referral, ends 01/07/2025 | Not live → not settled → no next accommodation → more than 12 weeks away | `CRS_UPCOMING_REFERRAL` |
| crs #42 | No referral, `TRANSIENT`, ends 01/07/2025 | `TRANSIENT` passes the settled check → too early | `CRS_UPCOMING_REFERRAL` |
| crs #19 | Sex `M`, no referral, ends 01/07/2025 | As #16, male | `CRS_UPCOMING_ACCOMMODATION_REFERRAL` |

## Inputs that determine the outcome

These are the scenario columns that feed the tree. Each maps to the `DomainData` field it populates:

| Scenario column | `DomainData` field |
|---|---|
| `referenceDate` | today, via the injected `Clock` |
| `sex` | `sex` (only `M` changes the outcome; blank means not recorded) |
| `hasNextAccommodation` | `nextAccommodations` (`TRUE` gives one next accommodation, `FALSE` an empty list) |
| `currentAccommodationEndDate` | `currentAccommodation.endDate` (blank means no current accommodation) |
| `currentAccommodationSettledType` | `currentAccommodationTypeEntity.settledType` (blank means no accommodation type) |
| `crsStatus` | `commissionedRehabilitativeServices.status` (blank means no referral) |

The table below lists every value the scenarios in `crs-eligibility-scenarios.csv` exercise. Update it when you add a scenario with a new value. _(blank)_ means the value is not recorded.

| Input field | Scenario file(s) | Values exercised by scenarios |
|---|---|---|
| `referenceDate` | `crs-eligibility-scenarios.csv` | `01/01/2025` |
| `sex` | `crs-eligibility-scenarios.csv` | _(blank)_, `M` |
| `hasNextAccommodation` | `crs-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
| `currentAccommodationEndDate` | `crs-eligibility-scenarios.csv` | _(blank)_, `01/01/2025`, `01/07/2025` |
| `currentAccommodationSettledType` | `crs-eligibility-scenarios.csv` | _(blank)_, `SETTLED`, `TRANSIENT` |
| `crsStatus` | `crs-eligibility-scenarios.csv` | _(blank)_, `COMPLETED`, `LIVE`, `WITHDRAWN` |
