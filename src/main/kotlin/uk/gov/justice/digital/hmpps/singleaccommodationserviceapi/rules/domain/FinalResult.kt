package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

data class FinalResult(
  val failedResults: List<RuleResult>,
  val ruleSetStatus: RuleSetStatus,
)

enum class RuleSetStatus(val value: String) {
  PASS("Pass"),
  FAIL("Fail"),
  GUIDANCE_FAIL("Guidance Fail"),
}
