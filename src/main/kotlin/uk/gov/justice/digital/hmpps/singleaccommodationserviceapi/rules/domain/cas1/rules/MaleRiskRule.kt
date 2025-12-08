package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

class MaleRiskRule : Rule {
  override val description = "FAIL if candidate is Male and is not Tier A3 - B1"
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.sex.code == "M" && !data.tier.matches(regex = Regex("^(A[1-3]|B[1-3])S?$"))) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    actionable = this.actionable,
  )
}
