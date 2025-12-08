package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

data class RuleSetResult(
  val results: List<RuleResult>,
  val ruleSetStatus: RuleSetStatus,
)

enum class RuleSetStatus {
  PASS,
  FAIL,
  ACTION_NEEDED,
}
