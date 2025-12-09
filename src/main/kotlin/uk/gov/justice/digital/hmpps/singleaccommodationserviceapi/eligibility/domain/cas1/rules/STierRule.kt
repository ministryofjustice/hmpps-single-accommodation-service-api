package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus

class STierRule : Rule {
  override val description = "FAIL if candidate is S Tier"
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.tier.endsWith("S")) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    actionable = this.actionable,
  )
}
