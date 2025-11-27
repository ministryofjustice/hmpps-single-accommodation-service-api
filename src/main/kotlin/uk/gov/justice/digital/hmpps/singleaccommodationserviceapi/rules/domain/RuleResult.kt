package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

data class RuleResult(
  val description: String,
  val ruleStatus: RuleStatus,
  val isGuidance: Boolean,
)

enum class RuleStatus(val value: String) {
  PASS("Pass"),
  FAIL("Fail"),
}
