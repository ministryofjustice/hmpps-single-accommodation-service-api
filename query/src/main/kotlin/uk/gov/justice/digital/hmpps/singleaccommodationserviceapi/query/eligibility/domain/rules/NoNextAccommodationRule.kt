package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus

@Component
class NoNextAccommodationRule : Rule {
  override val description = "FAIL if person has no future accommodation"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.nextAccommodations.isEmpty()) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
