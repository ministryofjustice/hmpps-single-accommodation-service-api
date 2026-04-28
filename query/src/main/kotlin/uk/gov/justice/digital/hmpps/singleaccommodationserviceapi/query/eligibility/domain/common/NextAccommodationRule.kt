package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.DtrEligibilityRule

@Component
class NextAccommodationRule :
  Cas3EligibilityRule,
  DtrEligibilityRule {
  override val description = "FAIL if candidate has next accommodation"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.hasNextAccommodation) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
