package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation.Cas3ValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRule

@Component
class CurrentAccommodationEndDateValidationRule :
  Cas3ValidationRule,
  Cas1ValidationRule,
  CrsEligibilityRule {
  override val description = "FAIL if candidate has no current accommodation end date"

  override fun evaluate(data: DomainData): RuleResult {
    val isFail = data.currentAccommodation?.endDate == null
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.NO_CURRENT_ACCOMMODATION_END_DATE else null,
    )
  }
}
