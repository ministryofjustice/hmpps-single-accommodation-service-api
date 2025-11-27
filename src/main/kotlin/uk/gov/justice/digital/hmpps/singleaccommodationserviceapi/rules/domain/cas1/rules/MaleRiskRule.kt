package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceType

class MaleRiskRule : Rule {
  override val services = listOf(ServiceType.CAS1)
  override val description = "Is Male and is Tier A3 - B1"
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.sex.code == "M" && data.tier.matches(regex = Regex("^[A-B].*"))) {
      RuleStatus.PASS
    } else {
      RuleStatus.FAIL
    },
  )
}
