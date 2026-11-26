# How the PPC rules graph determines a status

Date: 2026-11-25

## Purpose

This document explains how the PPC (Public protection checker) tree in the eligibility rules engine turns a person's data into one PPC status, and why the tree is ordered the way it is. The tree is built in `PpcEligibilityTreeProvider`; the interactive graph next to this panel is generated from that code, so the graph is always the source of truth for the exact shape.

## How to read the graph

- A **ruleset** node contains zero or more rules. If any rule fails, evaluation follows the **FAIL** edge; otherwise it follows the **PASS** edge. A ruleset with no rules always passes.
- **Naming:** a ruleset is named after what you need to pass it (for example `NotMaleRuleSet` passes when the person is not male). A rule is named after what makes it fail (for example `MaleRule` fails when the person is male). A ruleset and the rule inside it can therefore read as opposites; this is intentional.
- An **outcome** node ends evaluation with a fixed status, such as `PPC_NOT_STARTED`.
- This tree has no context updaters. It starts with no provisional status, and every path ends at an outcome node.
- The tree uses `PPC_NOT_REQUIRED` (meaning the checker does not need to be completed) rather than a "not eligible" status. None of the PPC rules set a failure reason, so every PPC result has an empty list of failure reasons.

Evaluation always starts at the top and stops at the first outcome it reaches, so **earlier checks take priority over later ones**.

## Evaluation order

The tree has two stages. Stage 2 takes one of two branches depending on the result of Stage 1.

### Stage 1: have the checker answers been submitted?

| Check | Data read | Result |
|---|---|---|
| PPC answers are present (not null) | `ppcAnswers` | go to Stage 2a |
| No PPC answers | `ppcAnswers` | go to Stage 2b |

**Why first:** the answers are the only input the tree uses. Until they exist, there is nothing to say about whether public protection is needed; the only question is whether the checker still has to be completed. Once they exist, the result is always a "completed" status.

### Stage 2a: do the answers identify a public protection need?

Reached only when answers are present. All three rules in `PpcPublicProtectionNotRequiredRuleSet` are evaluated; each fails if its answer is `true`:

| Check | Data read | Result |
|---|---|---|
| All three answers are `false` | `ppcAnswers.controlMeasureSuitable`, `ppcAnswers.supervisionRequired`, `ppcAnswers.practicalSupportSufficient` | `PPC_COMPLETED_PUBLIC_PROTECTION_NOT_REQUIRED` |
| Any one or more answers is `true` | as above | `PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED` |

**Why:**

- **Any `true` answer is enough.** The ruleset passes only when none of the three answers is `true`. The order of the three rules does not affect the result, because the outcome depends only on whether any of them fails and no failure reasons are recorded.
- **Not required is the narrow case.** Public protection is reported as not required only when every answer is `false`. Every other combination of answers reports it as required.
- **No null cases within the answers.** `PublicProtectionCheckerAnswers` holds three non-nullable booleans, so once answers are present each question has a definite answer.

### Stage 2b: must the checker be completed?

Reached only when there are no answers.

| Check | Data read | Result |
|---|---|---|
| `PpcRequiredRuleSet` passes | none | `PPC_NOT_STARTED` |
| `PpcRequiredRuleSet` fails | none | `PPC_NOT_REQUIRED` |

**Why:** this node separates "the checker should be started" from "the checker is not needed for this person". The PPC tree is a proof of concept. `PpcRequiredRuleSet` is a placeholder for the rules that decide who needs the checker; those rules will be added once the business has agreed them. Until then the ruleset has no rules, so it passes and every person without answers gets `PPC_NOT_STARTED`. The `PPC_NOT_REQUIRED` branch is already in place for when those rules arrive.

## Status summary

| Status | Reached when |
|---|---|
| `PPC_NOT_STARTED` | No PPC answers and `PpcRequiredRuleSet` passes (all cases until its rules are agreed) |
| `PPC_NOT_REQUIRED` | No PPC answers and `PpcRequiredRuleSet` fails (not returned until its rules are added) |
| `PPC_COMPLETED_PUBLIC_PROTECTION_NOT_REQUIRED` | Answers present and all three are `false` |
| `PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED` | Answers present and at least one is `true` |

`PPC_UPSTREAM_ERROR` also exists for PPC but is not produced by this tree.

## Worked examples

The PPC scenarios have no dates and no reference date, so there are no date boundaries. The CSV covers the "no answers" case and all eight combinations of the three answers. Every scenario expects an empty list of failure reasons.

| Scenario | Inputs | Path | Status |
|---|---|---|---|
| ppc #1 | No answers | Stage 1 fails → Stage 2b passes (placeholder ruleset) | `PPC_NOT_STARTED` |
| ppc #3 | `supervisionRequired` false, `controlMeasureSuitable` false, `practicalSupportSufficient` false | Stage 1 passes → no answer is `true` | `PPC_COMPLETED_PUBLIC_PROTECTION_NOT_REQUIRED` |
| ppc #4 | `supervisionRequired` false, `controlMeasureSuitable` false, `practicalSupportSufficient` true | Stage 1 passes → practical support rule fails | `PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED` |
| ppc #5 | `supervisionRequired` false, `controlMeasureSuitable` true, `practicalSupportSufficient` false | Stage 1 passes → control measure rule fails | `PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED` |
| ppc #6 | `supervisionRequired` true, `controlMeasureSuitable` false, `practicalSupportSufficient` false | Stage 1 passes → supervision rule fails | `PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED` |
| ppc #9 | All three answers true | Stage 1 passes → all three rules fail | `PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED` |

## Inputs that determine the outcome

These are the scenario columns that feed the tree. Each maps to the `DomainData` field it populates. The scenario test requires either all three columns to be filled in or all three to be blank:

| Scenario column | `DomainData` field |
|---|---|
| `supervisionRequired` | `ppcAnswers.supervisionRequired` (all three blank means `ppcAnswers` is null) |
| `controlMeasureSuitable` | `ppcAnswers.controlMeasureSuitable` (all three blank means `ppcAnswers` is null) |
| `practicalSupportSufficient` | `ppcAnswers.practicalSupportSufficient` (all three blank means `ppcAnswers` is null) |

The table below lists every value the scenarios in `ppc-eligibility-scenarios.csv` exercise. Update it when you add a scenario with a new value. _(blank)_ means the value is not recorded.

| Input field | Scenario file(s) | Values exercised by scenarios |
|---|---|---|
| `supervisionRequired` | `ppc-eligibility-scenarios.csv` | _(blank)_, `false`, `true` |
| `controlMeasureSuitable` | `ppc-eligibility-scenarios.csv` | _(blank)_, `false`, `true` |
| `practicalSupportSufficient` | `ppc-eligibility-scenarios.csv` | _(blank)_, `false`, `true` |
