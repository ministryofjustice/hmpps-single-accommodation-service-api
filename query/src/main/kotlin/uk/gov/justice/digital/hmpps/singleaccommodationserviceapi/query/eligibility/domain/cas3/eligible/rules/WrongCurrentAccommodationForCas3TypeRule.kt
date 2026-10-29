package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligible.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class WrongCurrentAccommodationForCas3TypeRule : Rule {
  override val description = "FAIL if person is not in CAS1, CAS2 or prison"

  override fun evaluate(data: DomainData): RuleResult {
    val isFail = data.currentAccommodationTypeEntity?.isPrison != true && data.currentAccommodationTypeEntity?.isCas1 != true && data.currentAccommodationTypeEntity?.isCas2 != true
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE else null,
    )
  }
}
