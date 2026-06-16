package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class DtrNotWithdrawnRule : Rule {
  override val description = "FAIL if DTR referral has been withdrawn"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.dutyToRefer?.status == DtrStatus.WITHDRAWN) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
