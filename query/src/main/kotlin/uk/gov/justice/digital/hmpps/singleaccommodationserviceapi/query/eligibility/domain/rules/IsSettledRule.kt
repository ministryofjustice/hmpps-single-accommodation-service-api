package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationTypeData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class IsSettledRule : Rule<AccommodationTypeData> {
  override val inputFields = setOf(DomainDataField.ACCOMMODATION_SETTLED_TYPE)
  override val description = "FAIL if person is settled"
  override val failureReason = FailureReason.IS_SETTLED

  override fun evaluate(data: AccommodationTypeData): RuleResult {
    val isFail = data.currentAccommodationTypeEntity?.settledType == AccommodationSettledType.SETTLED
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }
}
