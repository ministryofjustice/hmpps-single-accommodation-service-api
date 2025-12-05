package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

data class RuleResult(
  val description: String,
  val ruleStatus: RuleStatus,
  val isGuidance: Boolean,
  val potentialAction: String? = null,
)

enum class RuleStatus {
  PASS,
  FAIL,
}
