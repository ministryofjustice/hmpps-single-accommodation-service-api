# 02 RulesEngine surfaces per-rule results

## Issue

`RulesEngine.execute` was collapsing the per-rule evaluation into a single`RuleSetStatus` (`PASS` or `FAIL`) and throwing away everything else. The underlying `RuleSetEvaluator.evaluate` already produced a `List<RuleResult>`/ Lets log that somewhere

## What changed

- Added a `RuleSetEvaluation(status, results)` data class with a `failures` accessor.
- `RulesEngine.execute` now returns `RuleSetEvaluation` instead of `RuleSetStatus`.
- `RuleSetNode.eval` reads `evaluation.status` for branching and, on FAIL, logs the ruleset name plus the comma-separated descriptions of the failing rules at INFO.
- Updated `RulesEngineTest` and `DecisionNodeTest` to use the richer return type and to assert on `failures` where helpful.

No change to the underlying `RuleSetEvaluator` contract or to any rule.

## Value
1. Every FAIL event now has the failing rule descriptions in the log at INFO.