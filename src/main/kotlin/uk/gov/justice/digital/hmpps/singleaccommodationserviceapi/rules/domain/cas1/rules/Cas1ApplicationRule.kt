package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

class Cas1ApplicationRule : Rule {
  override val description = "FAIL if candidate has no submitted Cas1 application"
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.cas1Application?.submittedAt == null) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    isGuidance = this.isGuidance,
  )
}
