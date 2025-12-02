package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

class STierRule : Rule {
  override val description = "FAIL if candidate is S Tier"
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.tier.endsWith("S")) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    isGuidance = this.isGuidance,
  )
}
