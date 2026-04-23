package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class DtrPresentRule : DtrSuitabilityRule {
  override val description = "FAIL if DTR status is not present"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.dutyToRefer?.status == null) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
