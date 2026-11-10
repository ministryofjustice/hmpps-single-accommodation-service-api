package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationTypeData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class CurrentAccommodationTypeIsCommunityRule : Rule<AccommodationTypeData> {
  override val inputFields = setOf(DomainDataField.CURRENT_ACCOMMODATION_TYPE)
  override val description = "FAIL if person is in the community"
  override val failureReason = FailureReason.IN_COMMUNITY

  override fun evaluate(data: AccommodationTypeData): RuleResult {
    val isFail = data.currentAccommodationTypeEntity?.isPrison != true &&
      data.currentAccommodationTypeEntity?.isCas1 != true &&
      data.currentAccommodationTypeEntity?.isCas2 != true &&
      data.currentAccommodationTypeEntity?.isCas3 != true
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }
}
