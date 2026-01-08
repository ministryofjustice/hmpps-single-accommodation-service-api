package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction

data class RuleSetResult(
  val actions: List<RuleAction>,
  val ruleSetStatus: RuleSetStatus,
)

enum class RuleSetStatus {
  PASS,
  ACTIONABLE_FAIL,
  FAIL,
}
