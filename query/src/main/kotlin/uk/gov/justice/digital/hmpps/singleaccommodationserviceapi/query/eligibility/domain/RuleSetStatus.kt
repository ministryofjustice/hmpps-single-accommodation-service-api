package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

enum class RuleSetStatus {
  PASS,
  FAIL,
}

data class RuleSetEvaluation(
  val status: RuleSetStatus,
  val results: List<RuleResult>,
) {
  val failures: List<RuleResult> get() = results.filter { it.ruleStatus == RuleStatus.FAIL }
}
