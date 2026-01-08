package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction

interface Rule {
  val description: String
  val actionable: Boolean get() = false
  fun evaluate(data: DomainData): RuleResult
  fun buildAction(data: DomainData): RuleAction? = null
  fun actionWrapper(ruleStatus: RuleStatus, data: DomainData) = if (ruleStatus == RuleStatus.FAIL) { this.buildAction(data) } else null
}
