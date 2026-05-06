package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason

data class RuleResult(
  val description: String,
  val ruleStatus: RuleStatus,
  val failureReason: FailureReason? = null,
)

enum class RuleStatus {
  PASS,
  FAIL,
}
