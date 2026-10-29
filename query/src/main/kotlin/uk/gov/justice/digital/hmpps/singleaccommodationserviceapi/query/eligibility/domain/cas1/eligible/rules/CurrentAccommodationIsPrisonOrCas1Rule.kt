package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAccommodationIsPrisonOrCas1Rule : Rule {
  override val description = "FAIL if person is not in prison or CAS1"

  override fun evaluate(data: DomainData): RuleResult {
    val isFail = data.currentAccommodationTypeEntity?.isPrison != true &&
      data.currentAccommodationTypeEntity?.isCas1 != true
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.NOT_IN_PRISON_OR_CAS1_ACCOMMODATION else null,
    )
  }
}
