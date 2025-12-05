package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

class Cas1PlacementRule : Rule {
  override val description = "FAIL if candidate has no upcoming placement"
  override val isGuidance: Boolean = true
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.cas1Application?.status != ApprovedPremisesApplicationStatus.PlacementAllocated) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    isGuidance = this.isGuidance,
  )
}
