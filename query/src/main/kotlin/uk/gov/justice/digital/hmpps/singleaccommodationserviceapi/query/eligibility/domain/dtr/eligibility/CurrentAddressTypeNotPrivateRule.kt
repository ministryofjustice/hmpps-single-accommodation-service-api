package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAddressTypeNotPrivateRule : DtrEligibilityRule {
  override val description = "FAIL if current address is Private"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.currentAccommodation?.isPrivate == true) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
