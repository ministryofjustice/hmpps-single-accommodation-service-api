# How the DTR rules graph determines a status

Date: 2026-11-25

## Purpose

This document explains how the DTR (Duty to Refer) tree in the eligibility rules engine turns a person's data into one DTR status, and why the tree is ordered the way it is. The tree is built in `DtrEligibilityTreeProvider` (service name "Duty to Refer"); the interactive graph next to this panel is generated from that code, so the graph is always the source of truth for the exact shape.

## How to read the graph

- A **ruleset** node contains one or more rules. If any rule fails, evaluation follows the **FAIL** edge; otherwise it follows the **PASS** edge.
- **Naming:** a ruleset is named after what you need to pass it (for example `NotMaleRuleSet` passes when the person is not male). A rule is named after what makes it fail (for example `MaleRule` fails when the person is male). A ruleset and the rule inside it can therefore read as opposites; this is intentional.
- An **outcome** node ends evaluation with a fixed status, such as `DTR_ACCEPTED`.
- `DTR_UPCOMING` is produced by a context updater that ends evaluation straight away, so the graph shows it as an outcome. It also sets an action start date.
- This tree never returns a "not eligible" status. When a referral is not needed it returns `DTR_NOT_REQUIRED`, with the failure reason of the rule that failed (`HAS_NEXT_ACCOMMODATION`). No other DTR status carries a failure reason.

Evaluation always starts at the top and stops at the first outcome it reaches, so **earlier checks take priority over later ones**.

## Evaluation order

The tree has four stages. Each stage only runs if the previous stage did not produce a status.

### Stage 1: is there a live Duty to Refer?

All three rules must pass for the referral to count as live:

| Check | Data read | Result |
|---|---|---|
| A DTR status is recorded | `dutyToRefer.status` | missing: go to Stage 3 |
| The status is not `WITHDRAWN` | `dutyToRefer.status` | `WITHDRAWN`: go to Stage 3 |
| A submission date is recorded and is no more than 26 weeks before today | `dutyToRefer.submission.submissionDate`, today | missing or older: go to Stage 3 |
| All pass | | go to Stage 2 |

The expiry check passes when the submission date is on or after today minus 26 weeks, so a referral submitted exactly 26 weeks ago is still live. A missing submission date counts as expired.

**Why first:** this check runs before anything else, including the next accommodation and 8-week checks. A live referral is surfaced whatever else is true, so the practitioner can see the work already done. A withdrawn, expired or undated referral is treated as if there were no referral, and the tree goes on to decide whether a new one is needed.

### Stage 2: what outcome has the live referral reached?

| Check | Data read | Result |
|---|---|---|
| Status is `ACCEPTED` | `dutyToRefer.status` | `DTR_ACCEPTED` |
| Status is `NOT_ACCEPTED` | as above | `DTR_NOT_ACCEPTED` |
| Anything else (in practice `SUBMITTED`, as Stage 1 has removed missing and `WITHDRAWN`) | | `DTR_SUBMITTED` |

**Why:** a recorded outcome (accepted or not accepted) is more specific than "submitted", so it is checked first. `DTR_SUBMITTED` is the fallback for a live referral that is still waiting for an outcome.

### Stage 3: is a referral needed?

| Check | Data read | Result |
|---|---|---|
| The person has a next accommodation | `nextAccommodations` (not empty) | `DTR_NOT_REQUIRED` (`HAS_NEXT_ACCOMMODATION`) |
| No next accommodation | | go to Stage 4 |

**Why:** Stage 3 only runs when there is no live referral. If the person already has somewhere to go next, the tree does not ask for a new referral. This check comes after Stage 1, so it never hides a live referral: a person with a next accommodation and a live referral still sees the referral's status.

### Stage 4: is it too early to act?

| Check | Data read | Result |
|---|---|---|
| Current accommodation end date is more than 8 weeks after today | `currentAccommodation.endDate`, today | `DTR_UPCOMING`, with an action start date of the end date minus 8 weeks |
| Within 8 weeks (including dates in the past), or no end date | | `DTR_NOT_STARTED` |

**Why:**

- **Timing is checked last.** Timing only matters for someone who needs a new referral. Anyone with a live referral or a next accommodation has already got a status.
- **The boundary is inclusive.** An end date exactly 8 weeks away gives `DTR_NOT_STARTED`. One day later gives `DTR_UPCOMING`.
- **A missing end date counts as within 8 weeks.** The practitioner is shown the action rather than told to wait indefinitely.

## Status summary

| Status | Reached when |
|---|---|
| `DTR_ACCEPTED` | Live referral with status `ACCEPTED` |
| `DTR_NOT_ACCEPTED` | Live referral with status `NOT_ACCEPTED` |
| `DTR_SUBMITTED` | Live referral with status `SUBMITTED` |
| `DTR_NOT_REQUIRED` | No live referral (none, withdrawn, expired or no submission date) and a next accommodation exists |
| `DTR_UPCOMING` | No live referral, no next accommodation, end date more than 8 weeks away |
| `DTR_NOT_STARTED` | No live referral, no next accommodation, end date within 8 weeks or not recorded |

`DTR_UPSTREAM_ERROR` exists in `ServiceStatusV2` but is never returned by this tree.

## Worked examples

All rows in `dtr-eligibility-scenarios.csv` use a reference date of 01/01/2025. The 26-week expiry boundary is therefore 03/07/2024 (live) and 02/07/2024 (expired). The 8-week boundary is 26/02/2025 (`DTR_NOT_STARTED`) and 27/02/2025 (`DTR_UPCOMING`, action start date 02/01/2025).

| Scenario | Inputs | Path | Status |
|---|---|---|---|
| #2 | No referral, no next accommodation, ends 01/01/2025 | No referral → no next accommodation → within 8 weeks | `DTR_NOT_STARTED` |
| #1 | No referral, next accommodation, ends 01/01/2025 | No referral → next accommodation | `DTR_NOT_REQUIRED` (`HAS_NEXT_ACCOMMODATION`) |
| #13 | `SUBMITTED` on 04/07/2024, next accommodation | Live referral wins over next accommodation | `DTR_SUBMITTED` |
| #23 | `ACCEPTED` on 03/07/2024, next accommodation | Submitted exactly 26 weeks ago, still live | `DTR_ACCEPTED` |
| #32 | `NOT_ACCEPTED` on 03/07/2024, no next accommodation | Live → not accepted | `DTR_NOT_ACCEPTED` |
| #18 | `SUBMITTED` on 02/07/2024, no next accommodation, ends 01/01/2025 | Expired → no next accommodation → within 8 weeks | `DTR_NOT_STARTED` |
| #25 | `ACCEPTED` on 02/07/2024, next accommodation | Expired → next accommodation | `DTR_NOT_REQUIRED` (`HAS_NEXT_ACCOMMODATION`) |
| #20 | `ACCEPTED`, no submission date, no next accommodation, ends 01/01/2025 | Missing submission date counts as expired | `DTR_NOT_STARTED` |
| #104 | No referral, no next accommodation, ends 26/02/2025 | Exactly 8 weeks away, inclusive | `DTR_NOT_STARTED` |
| #138 | No referral, no next accommodation, ends 27/02/2025 | 8 weeks + 1 day away | `DTR_UPCOMING` |
| #150 | `SUBMITTED` on 04/07/2024, no next accommodation, ends 27/02/2025 | Live referral wins before timing is checked | `DTR_SUBMITTED` |
| #162 | `ACCEPTED` on 02/07/2024, no next accommodation, ends 27/02/2025 | Expired → no next accommodation → too early | `DTR_UPCOMING` |
| #70 | No referral, no next accommodation, ends 01/12/2024 | End date in the past counts as within 8 weeks | `DTR_NOT_STARTED` |
| #172 | No referral, no next accommodation, no end date | Missing end date counts as within 8 weeks | `DTR_NOT_STARTED` |
| #208 | `WITHDRAWN` on 04/07/2024, no next accommodation, ends 01/01/2025 | Withdrawn is not live, even with a recent submission | `DTR_NOT_STARTED` |
| #205 | `WITHDRAWN`, no submission date, next accommodation | Withdrawn → next accommodation | `DTR_NOT_REQUIRED` (`HAS_NEXT_ACCOMMODATION`) |
| #210 | `WITHDRAWN`, no submission date, no next accommodation, ends 27/02/2025 | Withdrawn → no next accommodation → too early | `DTR_UPCOMING` |

## Inputs that determine the outcome

These are the scenario columns that feed the tree. Each maps to the `DomainData` field it populates:

| Scenario column | `DomainData` field |
|---|---|
| `referenceDate` | today, via the injected `Clock` |
| `currentAccommodationEndDate` | `currentAccommodation.endDate` (blank means not recorded) |
| `hasNextAccommodation` | `nextAccommodations` (`TRUE` gives one entry, `FALSE` an empty list) |
| `dtrStatus` | `dutyToRefer.status` (blank means no `dutyToRefer` at all) |
| `dtrSubmissionDate` | `dutyToRefer.submission.submissionDate` (blank means no submission; ignored when `dtrStatus` is blank) |

The table below lists every value the scenarios in `dtr-eligibility-scenarios.csv` exercise. Update it when you add a scenario with a new value. _(blank)_ means the value is not recorded.

| Input field | Scenario file(s) | Values exercised by scenarios |
|---|---|---|
| `referenceDate` | `dtr-eligibility-scenarios.csv` | `01/01/2025` |
| `currentAccommodationEndDate` | `dtr-eligibility-scenarios.csv` | _(blank)_, `01/01/2025`, `01/02/2025`, `01/12/2024`, `26/02/2025`, `27/02/2025` |
| `hasNextAccommodation` | `dtr-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
| `dtrStatus` | `dtr-eligibility-scenarios.csv` | _(blank)_, `ACCEPTED`, `NOT_ACCEPTED`, `SUBMITTED`, `WITHDRAWN` |
| `dtrSubmissionDate` | `dtr-eligibility-scenarios.csv` | _(blank)_, `02/07/2024`, `03/07/2024`, `04/07/2024` |
