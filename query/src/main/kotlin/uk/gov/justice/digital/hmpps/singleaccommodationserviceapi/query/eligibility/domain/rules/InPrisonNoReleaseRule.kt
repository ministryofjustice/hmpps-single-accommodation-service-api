package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class InPrisonNoReleaseRule : Rule<AccommodationData> {
  override val inputFields = setOf(DomainDataField.CURRENT_ACCOMMODATION_TYPE, DomainDataField.CURRENT_ACCOMMODATION_END_DATE)
  override val description = "FAIL if person is in prison and has no release date"
  override val failureReason = FailureReason.IN_PRISON_NO_RELEASE_DATE

  override fun evaluate(data: AccommodationData): RuleResult {
    val isFail = data.currentAccommodationTypeEntity?.isPrison == true && data.currentAccommodation?.endDate == null
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }
}
