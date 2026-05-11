# 01 — Collapse constant ContextUpdaters into the tree builder

## 5 `ContextUpdater` classes are "constants": they take an `EvaluationContext` and returned a fixed `ServiceResult`

- `CommonContextUpdater` identity (returned `context.currentResult`).
- `CrsContextUpdater` fixed `ServiceResult(NOT_STARTED, action=COMPLETE_CRS_REFERRAL, link=VIEW_REFER_AND_MONITOR)`.
- `DtrSuitabilityContextUpdater` fixed `ServiceResult(NOT_STARTED, action=ADD_DTR_REFERRAL_DETAILS, link=ADD_REFERRAL_DETAILS)`.
- `DtrUpcomingContextUpdater` fixed `ServiceResult(UPCOMING)`.
- `PaCompletionContextUpdater` fixed `ServiceResult(NOT_STARTED, action=ADD_AND_CONFIRM_PROPOSED_ADDRESS)`.
  
`ContextUpdater` has become bigger than its implementation

## Changes 
- Added two factories
  - `ContextUpdater.constant(result)`
  - `ContextUpdater.identiy()`
- Added two overloads on `DecisionTreeBuilder`:
    - `ruleSet(name, ruleSet, onFailResult: ServiceResult)` uses `constant` internally.
    - `ruleSet(name, ruleSet)` uses `identity` internally.
- Fixed `EligibilityService` with the appropriate overload; 

## Why 

1. No file hopping, `ContextUpdater`'s default behaviour, is you just use the `identity` or `fail` one you only provide one if needed
2. 5 beans deleted, and 5 tests for these beans are deleted.
3. The "FAIL outcome is a fixed `ServiceResult`" case now has first-class support in the builder
4. After this change, every remaining `ContextUpdater` is a real function of `EvaluationContext`.