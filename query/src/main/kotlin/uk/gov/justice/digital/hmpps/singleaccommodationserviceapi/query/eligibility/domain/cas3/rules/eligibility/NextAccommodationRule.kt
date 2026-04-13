package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class NextAccommodationRule : Cas3EligibilityRule {
  override val description = "FAIL if candidate has next accommodation"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.hasNextAccommodation) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
