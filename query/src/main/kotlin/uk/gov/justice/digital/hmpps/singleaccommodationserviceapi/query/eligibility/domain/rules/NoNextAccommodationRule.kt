package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.NextAccommodationData

@Component
class NoNextAccommodationRule : Rule<NextAccommodationData> {
  override val inputFields = setOf(DomainDataField.NEXT_ACCOMMODATIONS)
  override val description = "FAIL if person has no future accommodation"

  override fun evaluate(data: NextAccommodationData) = RuleResult(
    description = description,
    ruleStatus = if (data.nextAccommodations.isEmpty()) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
