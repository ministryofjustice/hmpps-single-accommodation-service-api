package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.enums.RuleStatus

data class RuleResult(
  val description: String,
  val ruleStatus: RuleStatus,
  val actionable: Boolean,
  val potentialAction: String? = null,
)
