package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas1SubmittedRule : Rule {
  override val description = "FAIL if the referral is submitted"

  override fun evaluate(data: DomainData): RuleResult {
    val submittedStatuses = listOf(
      Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      Cas1ApplicationStatus.AWAITING_PLACEMENT,
      Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
      Cas1ApplicationStatus.REJECTED,
      Cas1ApplicationStatus.INAPPLICABLE,
      Cas1ApplicationStatus.WITHDRAWN,
      Cas1ApplicationStatus.EXPIRED,
    )

    val isFail = submittedStatuses.contains(data.cas1Application?.application?.status)

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
