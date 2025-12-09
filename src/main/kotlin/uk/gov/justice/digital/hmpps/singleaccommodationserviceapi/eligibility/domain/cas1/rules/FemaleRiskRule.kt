package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus

class FemaleRiskRule : Rule {
  override val description = "FAIL if candidate is Female and is not Tier A3 - C3"
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.sex?.code == "F" && !data.tier.matches(regex = Regex("^(A[1-3]|B[1-3]|C3)S?$"))) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    actionable = this.actionable,
  )
}
