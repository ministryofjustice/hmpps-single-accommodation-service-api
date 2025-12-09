package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleSetStatus

data class RuleSetResult(
  val results: List<RuleResult>,
  val ruleSetStatus: RuleSetStatus,
)
