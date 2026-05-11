package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class NoNextAccommodationRule : Rule {
  override val description = "FAIL if candidate has next accommodation"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.nextAccommodation != null) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
