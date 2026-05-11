package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas1ApplicationNotSuitableRule : PaEligibilityRule {
  override val description = "FAIL if candidate has a suitable CAS1 application"

  override fun evaluate(data: DomainData): RuleResult {
    val suitableStatuses = listOf(
      Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      Cas1ApplicationStatus.AWAITING_PLACEMENT,
      Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
    )

    val isFail = suitableStatuses.contains(data.cas1Application?.applicationStatus)

    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.SUITABLE_CAS1_APPLICATION else null,
    )
  }
}
