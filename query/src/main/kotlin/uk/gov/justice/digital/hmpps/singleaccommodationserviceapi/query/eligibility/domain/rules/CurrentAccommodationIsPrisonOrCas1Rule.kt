package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationTypeData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class CurrentAccommodationIsPrisonOrCas1Rule : Rule<AccommodationTypeData> {
  override val inputFields = setOf(DomainDataField.CURRENT_ACCOMMODATION_TYPE)
  override val description = "FAIL if person is not in prison or CAS1"
  override val failureReason = FailureReason.NOT_IN_PRISON_OR_CAS1_ACCOMMODATION

  override fun evaluate(data: AccommodationTypeData): RuleResult {
    val isFail = data.currentAccommodationTypeEntity?.isPrison != true &&
      data.currentAccommodationTypeEntity?.isCas1 != true
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }
}
