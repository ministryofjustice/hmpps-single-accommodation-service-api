# How the CAS3 rules graph determines a status

Date: 2026-11-25

## Purpose

This document explains how the CAS3 (Temporary accommodation) tree in the eligibility rules engine turns a person's data into one CAS3 status, and why the tree is ordered the way it is. The tree is built in `Cas3EligibilityTreeProvider`, whose service name is "Temporary accommodation". The interactive graph next to this panel is generated from that code, so the graph is always the source of truth for the exact shape.

## How to read the graph

- A **ruleset** node contains one or more rules. If any rule fails, evaluation follows the **FAIL** edge; otherwise it follows the **PASS** edge.
- **Naming:** a ruleset is named after what you need to pass it (for example `NotMaleRuleSet` passes when the person is not male). A rule is named after what makes it fail (for example `MaleRule` fails when the person is male). A ruleset and the rule inside it can therefore read as opposites; this is intentional.
- An **outcome** node ends evaluation with a fixed status, such as `CAS3_ARRIVED`.
- A **context updater** sets a provisional status, such as `CAS3_NOT_STARTED`, and evaluation continues. That provisional status is returned only if every later check passes.
- Some context updaters are terminal: they set the final status directly. `CAS3_UPCOMING` and the `CAS3_CANNOT_START_YET_*` statuses are set this way, replacing any provisional status.
- `CAS3_NOT_ELIGIBLE` is returned with the failure reason of the rule that failed. The `CAS3_CANNOT_START_YET_*` statuses keep the failure reasons of the DTR and CRS checks (`DTR_REFERRAL_EXPIRED`, `CRS_NOT_LIVE`). Every other status is returned without failure reasons.

Evaluation always starts at the top and stops at the first outcome it reaches, so **earlier checks take priority over later ones**.

## Evaluation order

The tree has five stages. Each stage only runs if the previous stage did not produce a status.

### Stage 1: is there a bedspace booking?

All booking data comes from `cas3Application.submittedApplication.latestBooking.status`. A booking with no status counts as no booking.

| Check | Data read | Result |
|---|---|---|
| No CAS3 application, no submitted application, no latest booking, or booking status not recorded | `cas3Application.submittedApplication.latestBooking.status` | go to Stage 2 |
| Booking status is `DEPARTED` or `CLOSED` | as above | set provisional `CAS3_NOT_STARTED_NEW`, go to Stage 3 |
| Otherwise | | continue below |

**Active booking** (status is not `DEPARTED` or `CLOSED`). The statuses are checked in this order:

| Booking status | Result |
|---|---|
| `CONFIRMED` | `CAS3_BOOKING_CONFIRMED` |
| `ARRIVED` | `CAS3_ARRIVED` |
| `PROVISIONAL` | `CAS3_BEDSPACE_OFFERED` |
| `NOT_MINUS_ARRIVED` | `CAS3_NOT_ARRIVED` |
| any other (in practice `CANCELLED`) | `CAS3_BOOKING_CANCELLED` |

**Why first:**

- **A booking is the most advanced piece of work.** If a bedspace has been offered, confirmed, arrived at, missed or cancelled, that booking is what the practitioner needs to see. The booking is checked before the assessment and application statuses, so an `ARRIVED` booking is shown even when the assessment is `CLOSED` (scenario 5).
- **Active bookings are not re-checked for eligibility.** These outcomes end evaluation immediately. An ineligible person with a `PROVISIONAL` booking still gets `CAS3_BEDSPACE_OFFERED`. A `CANCELLED` booking also ends here, as `CAS3_BOOKING_CANCELLED`.
- **A finished booking means "start again".** `DEPARTED` and `CLOSED` bookings set `CAS3_NOT_STARTED_NEW`, but only if the person passes the later stages.

### Stage 2: what stage has the referral reached?

Stage 2 runs only when there is no booking status. The checks run in this order:

| Order | Check | Data read | Result |
|---|---|---|---|
| 1 | Assessment status is `CLOSED` | `cas3Application.submittedApplication.assessmentStatus` | set provisional `CAS3_NOT_STARTED_NEW`, go to Stage 3 |
| 2 | Assessment status is `REJECTED`, **or** there is no assessment status and the application status is `REJECTED` | `assessmentStatus`, `cas3Application.applicationStatus` | set provisional `CAS3_REJECTED`, go to Stage 3 |
| 3 | The application has been submitted (a submitted application exists) | `cas3Application.submittedApplication` | `CAS3_SUBMITTED` |
| 4 | A CAS3 application exists (so it is `IN_PROGRESS`) | `cas3Application` | set provisional `CAS3_NOT_SUBMITTED`, go to Stage 3 |
| 5 | No CAS3 application | | set provisional `CAS3_NOT_STARTED`, go to Stage 3 |

A CAS3 application has a submitted application for every status except `IN_PROGRESS`, so check 3 passes for `SUBMITTED`, `REQUESTED_FURTHER_INFORMATION` and `REJECTED` applications that reach it.

**Why:**

- **Closed or rejected referrals are checked before "submitted".** Both mean the existing referral cannot progress, so the practitioner needs to start a new one. Checking them first stops a dead referral from being shown as `CAS3_SUBMITTED`.
- **Assessment status is always prioritised before application status.** The assessment is the later stage of a referral, so whenever an assessment status exists it decides the outcome and the application status is ignored. The application status is used only when there is no assessment. For example, an application with status `REJECTED` but an `IN_REVIEW` assessment is shown as `CAS3_SUBMITTED` (scenario 12).
- **Submitted referrals are not re-checked for eligibility.** `CAS3_SUBMITTED` ends evaluation immediately, so live work is not hidden behind `CAS3_NOT_ELIGIBLE`.
- **Every provisional status means "do something new".** That might be starting, continuing or re-referring. Asking someone to do that is only correct if they are eligible, can start, and the timing is right. Stages 3 to 5 decide that.

### Stage 3: is the person eligible for CAS3?

Stage 3 is reached only with a provisional status of `CAS3_NOT_STARTED`, `CAS3_NOT_STARTED_NEW`, `CAS3_NOT_SUBMITTED` or `CAS3_REJECTED`. The checks run in this order, and the first failure returns `CAS3_NOT_ELIGIBLE`:

| Order | Check | Data read | Failure reason |
|---|---|---|---|
| 1 | Current accommodation is a prison, CAS1 or CAS2 | `currentAccommodationTypeEntity.isPrison`, `.isCas1`, `.isCas2` | `INVALID_CURRENT_ACCOMMODATION_TYPE` |
| 2 | No next accommodation is recorded | `nextAccommodations` | `HAS_NEXT_ACCOMMODATION` |
| 3 | A ROSH level is recorded | `roshLevel` | `HAS_NO_ROSH_LEVEL` |

**Why:**

- **Current accommodation.** The tree only offers CAS3 to people currently in prison, CAS1 or CAS2 accommodation. Anyone else, including someone with no recorded accommodation type or already in CAS3, fails this check.
- **Next accommodation.** If a next accommodation is already recorded, the tree does not offer CAS3.
- **ROSH level.** The tree requires a recorded ROSH level before offering CAS3. Any recorded level passes.
- **Order of the checks.** Each check returns its own failure reason, and the first failure wins. Practitioners therefore see one reason at a time, current accommodation first.
- **Eligibility comes before the DTR and CRS checks.** An ineligible person sees "not eligible", rather than being asked to submit a DTR or CRS referral for a service they cannot use (scenario 275 in the eligibility CSV, with no ROSH level, an expired DTR and a withdrawn CRS referral, returns `HAS_NO_ROSH_LEVEL` only).

### Stage 4: can a referral be started yet?

| Check | Data read | Failure reason |
|---|---|---|
| A Duty to Refer (DTR) was submitted 26 weeks ago or less. A missing DTR or submission date fails | `dutyToRefer.submission.submissionDate`, today | `DTR_REFERRAL_EXPIRED` |
| There is a CRS referral with status `LIVE`. A missing referral fails | `commissionedRehabilitativeServices.status` | `CRS_NOT_LIVE` |

Both checks are in one ruleset, so both are always evaluated. If both pass, go to Stage 5. If either fails, the tree checks the person's sex (`sex`) and returns a "cannot start yet" status based on which checks failed:

| Failed checks | Sex is `M` | Any other value, including not recorded |
|---|---|---|
| DTR and CRS | `CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION` | `CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS` |
| CRS only | `CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION` | `CAS3_CANNOT_START_YET_SUBMIT_CRS` |
| DTR only | `CAS3_CANNOT_START_YET_SUBMIT_DTR` | `CAS3_CANNOT_START_YET_SUBMIT_DTR` |

**Why:**

- **Prerequisites after eligibility.** These checks only matter for someone who is eligible. They tell the practitioner which prerequisite is missing before a CAS3 referral can start.
- **Both failures are reported together.** Because the DTR and CRS checks run in the same ruleset, the practitioner sees everything that is missing in one go rather than one item at a time.
- **The status depends on sex.** For male people, the CRS-related statuses are the `_ACCOMMODATION` variants. There is no male-specific variant for a DTR-only failure, so both branches return `CAS3_CANNOT_START_YET_SUBMIT_DTR`. Unknown sex follows the non-male branch.
- **The "cannot start" status replaces the provisional status.** This applies to every provisional status, including `CAS3_NOT_SUBMITTED` and `CAS3_REJECTED`. It also takes priority over the timing check in Stage 5 (scenario 1357).

### Stage 5: is it too early to act?

| Check | Data read | Result |
|---|---|---|
| Current accommodation end date is more than 15 days after today | `currentAccommodation.endDate`, today | `CAS3_UPCOMING`, with an action start date of the end date minus 15 days |
| Within 15 days, or no end date | | the provisional status from Stage 1 or Stage 2 |

**Why:**

- **Timing is checked last.** Timing only matters for someone who is eligible and can start a referral. An ineligible person should see "not eligible", and someone missing a DTR or CRS referral should see what to submit, not "come back later".
- **It also applies to rejected, closed and unsubmitted referrals.** The practitioner is not expected to (re)refer yet, so `CAS3_UPCOMING` replaces the provisional status. It also tells them when to come back.
- **Missing end date counts as within 15 days.** This is a fail-open choice: the practitioner is shown the action rather than told to wait indefinitely.

## Status summary

| Status | Reached when |
|---|---|
| `CAS3_BOOKING_CONFIRMED` | Booking `CONFIRMED` (Stage 1) |
| `CAS3_ARRIVED` | Booking `ARRIVED` (Stage 1) |
| `CAS3_BEDSPACE_OFFERED` | Booking `PROVISIONAL` (Stage 1) |
| `CAS3_NOT_ARRIVED` | Booking `NOT_MINUS_ARRIVED` (Stage 1) |
| `CAS3_BOOKING_CANCELLED` | Any other active booking status, in practice `CANCELLED` (Stage 1) |
| `CAS3_SUBMITTED` | No booking, assessment not `CLOSED` or rejected, application submitted (Stage 2) |
| `CAS3_NOT_STARTED_NEW` | Booking `DEPARTED` or `CLOSED`, or no booking and assessment `CLOSED`; passes Stages 3 to 5 |
| `CAS3_REJECTED` | No booking and the referral was rejected; passes Stages 3 to 5 |
| `CAS3_NOT_SUBMITTED` | Application `IN_PROGRESS`; passes Stages 3 to 5 |
| `CAS3_NOT_STARTED` | No CAS3 application; passes Stages 3 to 5 |
| `CAS3_UPCOMING` | Would be one of the previous four, but the end date is more than 15 days away |
| `CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION` | Would be one of the provisional statuses or `CAS3_UPCOMING`, but the DTR and CRS checks both failed and sex is `M` |
| `CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS` | As above, but sex is not `M` or not recorded |
| `CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION` | Would be one of the provisional statuses or `CAS3_UPCOMING`, but only the CRS check failed and sex is `M` |
| `CAS3_CANNOT_START_YET_SUBMIT_CRS` | As above, but sex is not `M` or not recorded |
| `CAS3_CANNOT_START_YET_SUBMIT_DTR` | Would be one of the provisional statuses or `CAS3_UPCOMING`, but only the DTR check failed (any sex) |
| `CAS3_NOT_ELIGIBLE` | Would be a provisional status, `CAS3_UPCOMING` or a `CAS3_CANNOT_START_YET_*` status, but a Stage 3 check failed |

## Worked examples

Both scenario CSVs use a reference date of 01/01/2025. The 15-day boundary is 16/01/2025: an end date of 16/01/2025 is within 15 days, and 17/01/2025 is `CAS3_UPCOMING`. The 26-week DTR boundary is 03/07/2024: a submission on 03/07/2024 passes, and 02/07/2024 has expired.

In `cas3-application-scenarios.csv`, every person is male, has ROSH level `LOW`, has no next accommodation, and has a DTR submitted on the reference date. "Eligible" means the current accommodation is a prison (`HMP`); "ineligible" means it is CAS3 accommodation. "Started" means the CRS referral is `LIVE`; "not started" means it is `WITHDRAWN`.

In `cas3-eligibility-scenarios.csv`, there is no CAS3 application, so the provisional status is always `CAS3_NOT_STARTED`. Each testCaseId appears once per ROSH level (not recorded, `LOW`, `MEDIUM`, `HIGH`, `VERY_HIGH`), so the ROSH level is given with each example.

| Scenario | Inputs | Path | Status |
|---|---|---|---|
| application #376 | Eligible, started, no application, no end date | No booking → no application → `NOT_STARTED` → Stages 3 to 5 pass | `CAS3_NOT_STARTED` |
| application #876 | As #376, ends 16/01/2025 | As above; end date exactly 15 days away | `CAS3_NOT_STARTED` |
| application #1376 | As #376, ends 17/01/2025 | As above, but the end date is 16 days away | `CAS3_UPCOMING` |
| application #377 | Eligible, started, application `IN_PROGRESS` | Not submitted → `NOT_SUBMITTED` → Stages 3 to 5 pass | `CAS3_NOT_SUBMITTED` |
| application #378 | Eligible, started, application `REJECTED`, no assessment | No assessment and application rejected → `REJECTED` → Stages 3 to 5 pass | `CAS3_REJECTED` |
| application #485 | Eligible, started, application `SUBMITTED`, assessment `REJECTED` | Assessment rejected → `REJECTED` → Stages 3 to 5 pass | `CAS3_REJECTED` |
| application #12 | Ineligible, not started, application `REJECTED`, assessment `IN_REVIEW` | Assessment present and not rejected → submitted; eligibility not checked | `CAS3_SUBMITTED` |
| application #461 | Eligible, started, application `SUBMITTED`, assessment `CLOSED` | Assessment closed → `NOT_STARTED_NEW` → Stages 3 to 5 pass | `CAS3_NOT_STARTED_NEW` |
| application #5 | Ineligible, not started, application `REJECTED`, assessment `CLOSED`, booking `ARRIVED` | Booking checked before assessment; eligibility not checked | `CAS3_ARRIVED` |
| application #104 | Ineligible, not started, application `SUBMITTED`, assessment `READY_TO_PLACE`, booking `CANCELLED` | Active booking, not confirmed, arrived, provisional or not arrived | `CAS3_BOOKING_CANCELLED` |
| application #482 | Eligible, started, application `SUBMITTED`, assessment `READY_TO_PLACE`, booking `DEPARTED` | Departed → `NOT_STARTED_NEW` → Stages 3 to 5 pass | `CAS3_NOT_STARTED_NEW` |
| application #230 | Ineligible, started, application `SUBMITTED`, assessment `READY_TO_PLACE`, booking `CLOSED` | Closed → `NOT_STARTED_NEW` → CAS3 accommodation fails | `CAS3_NOT_ELIGIBLE` |
| application #357 | Eligible, not started, application `SUBMITTED`, assessment `READY_TO_PLACE`, booking `DEPARTED` | Departed → `NOT_STARTED_NEW` → eligible → CRS not live, male | `CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION` |
| application #1357 | As #357, ends 17/01/2025 | As above; Stage 4 wins before the timing check | `CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION` |
| application #1482 | As #482, ends 17/01/2025 | Departed → `NOT_STARTED_NEW` → eligible → can start → too early | `CAS3_UPCOMING` |
| eligibility #284 (ROSH `LOW`) | `HMP`, no next accommodation, DTR 03/07/2024, CRS `LIVE`, sex `M`, no end date | All checks pass; DTR exactly 26 weeks old | `CAS3_NOT_STARTED` |
| eligibility #320 (ROSH `LOW`) | As #284, ends 16/01/2025 | Within 15 days | `CAS3_NOT_STARTED` |
| eligibility #356 (ROSH `LOW`) | As #284, ends 17/01/2025 | More than 15 days away | `CAS3_UPCOMING` |
| eligibility #392 (ROSH `LOW`) | Not prison, CAS1 or CAS2, otherwise as #284 | Current accommodation fails | `CAS3_NOT_ELIGIBLE` (`INVALID_CURRENT_ACCOMMODATION_TYPE`) |
| eligibility #752 (ROSH `LOW`) | As #284, with a next accommodation | Next accommodation fails | `CAS3_NOT_ELIGIBLE` (`HAS_NEXT_ACCOMMODATION`) |
| eligibility #275 (ROSH not recorded) | `HMP`, no next accommodation, DTR 02/07/2024, CRS `WITHDRAWN`, sex `M` | ROSH fails before DTR and CRS are checked | `CAS3_NOT_ELIGIBLE` (`HAS_NO_ROSH_LEVEL`) |
| eligibility #272 (ROSH `LOW`) | `HMP`, no next accommodation, DTR 02/07/2024, CRS `LIVE`, sex `M` | DTR expired | `CAS3_CANNOT_START_YET_SUBMIT_DTR` (`DTR_REFERRAL_EXPIRED`) |
| eligibility #71 (ROSH `HIGH`) | `A02`, no next accommodation, DTR 03/07/2024, CRS `WITHDRAWN`, sex `M` | CRS not live, male | `CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION` (`CRS_NOT_LIVE`) |
| eligibility #72 (ROSH `HIGH`) | As #71, sex not `M` | CRS not live, not male | `CAS3_CANNOT_START_YET_SUBMIT_CRS` (`CRS_NOT_LIVE`) |
| eligibility #218 (ROSH `MEDIUM`) | CAS2 (`A10`/`A11`), no next accommodation, no DTR, no CRS, sex `M`, ends 17/01/2025 | DTR and CRS fail, male; Stage 4 wins before timing | `CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION` (`DTR_REFERRAL_EXPIRED`, `CRS_NOT_LIVE`) |
| eligibility #217 (ROSH `MEDIUM`) | As #218, sex not recorded | DTR and CRS fail, unknown sex follows the non-male branch | `CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS` (`DTR_REFERRAL_EXPIRED`, `CRS_NOT_LIVE`) |

## Inputs that determine the outcome

These are the scenario columns that feed the tree. Each maps to the `DomainData` field it populates:

| Scenario column | `DomainData` field |
|---|---|
| `cas3ApplicationStatus` | `cas3Application.applicationStatus` (blank means no application; any status other than `IN_PROGRESS` also creates `cas3Application.submittedApplication`) |
| `cas3AssessmentStatus` | `cas3Application.submittedApplication.assessmentStatus` (blank means no assessment) |
| `cas3BookingStatus` | `cas3Application.submittedApplication.latestBooking.status` (blank means no booking) |
| `isEligible` | `currentAccommodationTypeEntity` and `currentAccommodation.type`: `TRUE` is a prison (`HMP`), which passes Stage 3; `FALSE` is CAS3 accommodation, which fails it |
| `isStarted` | `commissionedRehabilitativeServices.status`: `TRUE` is `LIVE`, `FALSE` is `WITHDRAWN` |
| `currentAccommodationType` | `currentAccommodationTypeEntity` and `currentAccommodation.type` (`HMP` is a prison, `A02` is CAS1, `A10-A11` runs once each for the CAS2 types `A10` and `A11`, `notPrisonCas1OrCas2` runs once for each type that is none of these, blank means no current accommodation) |
| `currentAccommodationEndDate` | `currentAccommodation.endDate` |
| `hasNextAccommodation` | `nextAccommodations` (`TRUE` adds one next accommodation, `FALSE` leaves it empty) |
| `RiskLevel` | `roshLevel` (blank means not recorded) |
| `dtrSubmissionDate` | `dutyToRefer.submission.submissionDate` (blank means no DTR) |
| `crsStatus` | `commissionedRehabilitativeServices.status` (blank means no CRS referral) |
| `sexCode` | `sex` (`notM` runs once each for `N`, `F` and `NS`; blank means not recorded) |
| `referenceDate` | today, via the injected `Clock` |

The table below lists every value the scenarios in `cas3-eligibility-scenarios.csv` and `cas3-application-scenarios.csv` exercise. Update it when you add a scenario with a new value. _(blank)_ means the value is not recorded.

| Input field | Scenario file(s) | Values exercised by scenarios |
|---|---|---|
| `referenceDate` | `cas3-eligibility-scenarios.csv`<br>`cas3-application-scenarios.csv` | `01/01/2025` |
| `RiskLevel` | `cas3-eligibility-scenarios.csv` | _(blank)_, `HIGH`, `LOW`, `MEDIUM`, `VERY_HIGH` |
| `hasNextAccommodation` | `cas3-eligibility-scenarios.csv` | `FALSE`, `TRUE` |
| `currentAccommodationType` | `cas3-eligibility-scenarios.csv` | _(blank)_, `A02`, `A10-A11`, `HMP`, `notPrisonCas1OrCas2` |
| `currentAccommodationEndDate` | `cas3-eligibility-scenarios.csv`<br>`cas3-application-scenarios.csv` | _(blank)_, `16/01/2025`, `17/01/2025` |
| `dtrSubmissionDate` | `cas3-eligibility-scenarios.csv` | _(blank)_, `02/07/2024`, `03/07/2024` |
| `crsStatus` | `cas3-eligibility-scenarios.csv` | _(blank)_, `LIVE`, `WITHDRAWN` |
| `sexCode` | `cas3-eligibility-scenarios.csv` | _(blank)_, `M`, `notM` |
| `isEligible` | `cas3-application-scenarios.csv` | `FALSE`, `TRUE` |
| `isStarted` | `cas3-application-scenarios.csv` | `FALSE`, `TRUE` |
| `cas3ApplicationStatus` | `cas3-application-scenarios.csv` | _(blank)_, `IN_PROGRESS`, `REJECTED`, `REQUESTED_FURTHER_INFORMATION`, `SUBMITTED` |
| `cas3AssessmentStatus` | `cas3-application-scenarios.csv` | _(blank)_, `CLOSED`, `IN_REVIEW`, `READY_TO_PLACE`, `REJECTED`, `UNALLOCATED` |
| `cas3BookingStatus` | `cas3-application-scenarios.csv` | _(blank)_, `ARRIVED`, `CANCELLED`, `CLOSED`, `CONFIRMED`, `DEPARTED`, `NOT_MINUS_ARRIVED`, `PROVISIONAL` |
